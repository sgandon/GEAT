package org.talend.geat;

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

import com.google.common.base.Strings;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class GeatMain {

    public static void main(String[] args) {
        String workingDir = System.getProperty("user.dir");

        if (args.length == 1 && args[0].equals("dev")) {
            args = new String[] { "feature-finish", "tagada", "f" };
            workingDir = "/tmp/repo-test";
        }

        SanityCheck.check(workingDir, CheckLevel.GIT_REPO_ONLY, true, true);

        initSsh();

        if (args.length < 1) {
            usage();
        }

        Command command = CommandsRegistry.INSTANCE.getCommand(args[0]);
        if (command == null) {
            usage();
        }

        if (command.getArgsNumber() != args.length - 1) {
            System.out.println("Wrong number of parameters for this command!\nUsage is:\n");
            System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + args[0] + " "
                    + command.getUsage());
            System.exit(1);
        }

        command.setWorkingDir(workingDir).run(args);
    }

    private static void usage() {
        CommandsRegistry.INSTANCE.getCommand("help").run(null);
        System.exit(1);
    }

    private static void initSsh() {
        final String sshPassphrase = InputsUtils.askUser("SSH passphrase (will not be stored), leave empty to skip",
                null);

        if (sshPassphrase != null) {
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
        } else {
            System.out.println("WARN: SSH not set.");
        }
    }

}
