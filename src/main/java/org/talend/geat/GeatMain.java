package org.talend.geat;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProviderUserInfo;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.talend.geat.commands.Command;
import org.talend.geat.commands.CommandsRegistry;
import org.talend.geat.commands.Help;
import org.talend.geat.commands.Version;
import org.talend.geat.exception.IncorrectRepositoryStateException;
import org.talend.geat.exception.InterruptedCommandException;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class GeatMain {

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("dev")) {
            args = new String[] { "help" };
            args = new String[] { "feature-start", "tagada" };
            args = new String[] { "feature-finish", "config" };
            System.setProperty("user.dir", "/tmp/test");
        }

        try {
            if (GitUtils.hasRemote("origin", Git.open(new File(System.getProperty("user.dir"))).getRepository())) {
                initSsh();
            }
        } catch (IOException e) {
            // Should not occurs (check above in SanityCheck)
        }

        if (args.length < 1) {
            usage();
        }

        Command command = CommandsRegistry.INSTANCE.getCommand(args[0]);
        if (command == null) {
            usage();
        }

        try {
            command.parseArgs(args).run();
        } catch (IncorrectRepositoryStateException e) {
            System.out.println(e.getDetails());
        } catch (InterruptedCommandException e) {
            System.out.println(e.getDetails());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("");
    }

    private static void usage() {
        try {
            CommandsRegistry.INSTANCE.getCommand(Version.NAME).run();
            System.out.println("\n");
            CommandsRegistry.INSTANCE.getCommand(Help.NAME).run();
        } catch (Exception e) {
            // Should not occurs
        }
        System.out.println("");
        System.exit(1);
    }

    private static void initSsh() {
        String sshPassphrase = GitConfiguration.getInstance().get("sshpassphrase");
        if (sshPassphrase == null) {
            sshPassphrase = InputsUtils.askUser("SSH passphrase, leave empty to skip", null);
            if (InputsUtils.askUserAsBoolean("Do you want to save this passphrase in your local gitconfig file")) {
                GitConfiguration.getInstance().set(GitConfiguration.CONFIG_PREFIX, "sshpassphrase", sshPassphrase);
            }
        }

        if (sshPassphrase != null) {
            setSshPassphrase(sshPassphrase);
        } else {
            System.out.println("WARN: SSH not set.");
        }
    }

    private static void setSshPassphrase(final String sshPassphrase) {
        JschConfigSessionFactory sessionFactory = new JschConfigSessionFactory() {

            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
                CredentialsProvider provider = new CredentialsProvider() {

                    @Override
                    public boolean isInteractive() {
                        return false;
                    }

                    @Override
                    public boolean supports(CredentialItem... items) {
                        return true;
                    }

                    @Override
                    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
                        for (CredentialItem item : items) {
                            ((CredentialItem.StringType) item).setValue(sshPassphrase);
                        }
                        return true;
                    }
                };
                UserInfo userInfo = new CredentialsProviderUserInfo(session, provider);
                session.setUserInfo(userInfo);
            }
        };
        SshSessionFactory.setInstance(sessionFactory);
    }

}
