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
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.commands.Command;
import org.talend.geat.commands.CommandsRegistry;
import org.talend.geat.commands.Help;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class GeatMain {

    public static void main(String[] args) {
        String workingDir = System.getProperty("user.dir");

        if (args.length == 1 && args[0].equals("dev")) {
            args = new String[] { "help" };
            args = new String[] { "feature-start", "tagada" };
            args = new String[] { "feature-finish", "config" };
            // workingDir = "/tmp/test";
        }

        SanityCheck.check(workingDir, CheckLevel.GIT_REPO_ONLY, true, true);

        try {
            Configuration.setInstance(workingDir);

            if (GitUtils.hasRemote("origin", Git.open(new File(workingDir)).getRepository())) {
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

        command.setWorkingDir(workingDir).parseArgs(args).run();
    }

    private static void usage() {
        CommandsRegistry.INSTANCE.getCommand(Help.NAME).run();
        System.exit(1);
    }

    private static void initSsh() {
        String sshPassphrase = Configuration.getInstance().get("sshpassphrase");
        if (sshPassphrase == null) {
            sshPassphrase = InputsUtils.askUser("SSH passphrase, leave empty to skip", null);
            if (InputsUtils.askUserAsBoolean("Do you want to save this passphrase in your local gitconfig file")) {
                Configuration.getInstance().set(Configuration.CONFIG_PREFIX, "sshpassphrase", sshPassphrase);
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
