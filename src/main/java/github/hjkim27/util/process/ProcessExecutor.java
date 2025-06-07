package github.hjkim27.util.process;


import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessExecutor {

    /**
     * RUN just input command
     *
     * @param command exec command
     * @return stdout as single String
     * @throws IOException
     * @throws InterruptedException
     */
    public static String runSimpleCommand(String command) throws IOException, InterruptedException {
        String[] arr = {command};
        return runSimpleCommand(arr);
    }

    /**
     * <PRE>
     * RUN input command
     * (prefix :  sh -c )
     * </PRE>
     *
     * @param command exec command
     * @return stdout as single String
     * @throws IOException
     * @throws InterruptedException
     */
    public static String runSimpleCommandShell(String command) throws IOException, InterruptedException {
        String[] arr = {"sh", "-c", command};
        return runSimpleCommand(arr);
    }

    /**
     * <PRE>
     * RUN just input command
     * </PRE>
     *
     * @param command exec command
     * @return stdout as String List
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<String> runCommand(String command) throws IOException, InterruptedException {
        String[] arr = {command};
        return runCommand(arr);
    }


    /**
     * <PRE>
     * RUN input command
     * (prefix :  sh -c )
     * </PRE>
     *
     * @param command exec command
     * @return stdout as String List
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<String> runCommandShell(String command) throws IOException, InterruptedException {
        String[] arr = {"sh", "-c", command};
        return runCommand(arr);
    }


    public static String runSimpleCommand(String[] command) throws IOException, InterruptedException {
        List<String> list = runCommand(command);
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s + "\n");
        }
        return sb.toString();
    }

    public static List<String> runCommand(String[] command) throws IOException, InterruptedException {

        Process process = null;

        List<String> standardOut = new ArrayList<>();
        List<String> errorOut = new ArrayList<>();

        try {
            if (command == null || command.length == 0) {
                throw new NullPointerException("exec command is null");
            }

            log.debug("run command : {}", new Gson().toJson(command));

            if (command.length == 1) {
                process = Runtime.getRuntime().exec(command[0]);
            } else {
                process = Runtime.getRuntime().exec(command);
            }

            if (process != null) {
                try (
                        InputStream is1 = process.getInputStream();
                        InputStream is2 = process.getErrorStream();
                        InputStreamReader inputReader = new InputStreamReader(is1);
                        InputStreamReader errorReader = new InputStreamReader(is2);
                        BufferedReader stdReader = new BufferedReader(inputReader);
                        BufferedReader errReader = new BufferedReader(errorReader);
                ) {
                    String line = null;

                    while ((line = stdReader.readLine()) != null) {
                        standardOut.add(line);
                    }

                    line = null;
                    while ((line = errReader.readLine()) != null) {
                        errorOut.add(line);
                    }
                }
            }

            process.waitFor();
            log.debug("exitValue : {}", process.exitValue());
            if (process.exitValue() != 0) {
                throw new IOException("Exit Code is not normal");
            }

            if (log.isDebugEnabled()) {
                for (String s : standardOut) {
                    log.debug(s);
                }
            }

        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
            for (String s : errorOut) {
                log.warn("\t{}", s);
            }
            throw e;
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            for (String s : errorOut) {
                log.warn("\t{}", s);
            }
            throw new IOException(e);
        } catch (Exception e) {
            log.error("Unexpected Error", e);
            for (String s : errorOut) {
                log.warn("\t{}", s);
            }
            throw new IOException(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return standardOut;
    }


}