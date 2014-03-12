package org.talend.geat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProviderUserInfo;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.talend.geat.commands.Command;
import org.talend.geat.commands.FeatureFinish;
import org.talend.geat.commands.FeatureStart;

import com.google.common.base.Strings;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class GeatMain {

    private static void usage(Map<String, Command> commands) {
        System.out.println("Available commands are:");
        for (Entry<String, Command> command : commands.entrySet()) {
            System.out.println(" - " + command.getKey() + " - " + command.getValue().getDescription());
        }
    }

    public static void main(String[] args) {
        String workingDir = System.getProperty("user.dir");

        initSsh();

        if (args.length == 1 && args[0].equals("dev")) {
            args = new String[] { "feature-finish", "tagada", "f" };
            workingDir = "/tmp/repo-test";
        }

        Map<String, Command> commands = new HashMap<String, Command>();
        commands.put("feature-start", new FeatureStart());
        commands.put("feature-finish", new FeatureFinish());

        if (args.length < 1 || !commands.containsKey(args[0])) {
            usage(commands);
            System.exit(1);
        }
        Command command = commands.get(args[0]);

        if (command.getArgsNumber() != args.length - 1) {
            System.out.println("Wrong number of parameters for this command!\nUsage is:\n");
            System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates) + args[0] + " "
                    + command.getUsage());
            System.exit(1);
        }

        command.setWorkingDir(workingDir).run(args);
    }

    private static void initSsh() {
        try {
            final String sshPassphrase = InputsUtils.askUser(
                    "SSH passphrase (will not be stored), leave empty to skip", null);

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
        } catch (IOException e) {
            System.out.println("WARN: Cannot read SSH passphrase.");
        }
    }

}
