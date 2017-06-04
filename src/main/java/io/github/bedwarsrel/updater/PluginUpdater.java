package io.github.bedwarsrel.updater;

import io.github.bedwarsrel.BedwarsRel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Check for updates on BukkitDev for a given plugin, and download the updates if needed.
 * <p>
 * <b>VERY, VERY IMPORTANT</b>: Because there are no standards for adding auto-update toggles in
 * your plugin's config, this system provides NO CHECK WITH YOUR CONFIG to make sure the user has
 * allowed auto-updating. <br>
 * It is a <b>BUKKIT POLICY</b> that you include a boolean value in your config that prevents the
 * auto-updater from running <b>AT ALL</b>. <br>
 * If you fail to include this option in your config, your plugin will be <b>REJECTED</b> when you
 * attempt to submit it to dev.bukkit.org.
 * </p>
 * An example of a good configuration option would be something similar to 'auto-update: true' - if
 * this value is set to false you may NOT run the auto-updater. <br>
 * If you are unsure about these rules, please read the plugin submission guidelines:
 * http://goo.gl/8iU5l
 *
 * @author Gravity
 * @version 2.3
 */

public class PluginUpdater {

  /* Constants */

  // Config key for api key
  private static final String API_KEY_CONFIG_KEY = "api-key";
  // Default api key value in config
  private static final String API_KEY_DEFAULT = "c9012155bcd01874f55bfd2f38de962dc0dc353a";
  // Used for downloading files
  private static final int BYTE_SIZE = 1024;
  // Config key for disabling updater
  private static final String DISABLE_CONFIG_KEY = "disable";
  // Default disable value in config
  private static final boolean DISABLE_DEFAULT = false;
  // Slugs will be appended to this to get to the project's RSS feed
  private static final String HOST = "https://api.curseforge.com";
  // Remote file's download link
  private static final String LINK_VALUE = "downloadUrl";
  // Path to GET
  private static final String QUERY = "/servermods/files?projectIds=";
  // Remote file's title
  private static final String TITLE_VALUE = "name";
  // Remote file's release type
  private static final String TYPE_VALUE = "releaseType";
  // User-agent when querying Curse
  private static final String USER_AGENT = "Bedwars Plugin updater";
  // Remote file's build version
  private static final String VERSION_VALUE = "gameVersion";

  /* User-provided variables */
  // Whether to announce file downloads
  private final boolean announce;
  // The provided callback (if any)
  private final UpdateCallback callback;
  // The plugin file (jar)
  private final File file;
  // Plugin running updater
  private final Plugin plugin;
  // Type of update check to run
  private final UpdateType type;
  // The folder that downloads will be placed in
  private final File updateFolder;
  // BukkitDev ServerMods API key
  private String apiKey = null;
  // Project's Curse ID
  private int id = -1;

  /* Collected from Curse API */
  private boolean lastVersionCheck = false;
  // Used for determining the outcome of the update process
  private PluginUpdater.UpdateResult result = PluginUpdater.UpdateResult.SUCCESS;
  // updater thread
  private Thread thread;
  // Connection to RSS
  private URL url;
  private String versionCustom;
  private String versionGameVersion;

  /* Update process variables */
  private String versionLink;
  private String versionName;
  private String versionType;

  /**
   * Initialize the updater.
   *
   * @param plugin The plugin that is checking for an update.
   * @param id The dev.bukkit.org id of the project.
   * @param file The file that the plugin is running from, get this by doing this.getFile() from
   * within your main class.
   * @param type Specify the type of update this will be. See {@link UpdateType}
   * @param announce True if the program should announce the progress of new updates in console.
   */
  public PluginUpdater(Plugin plugin, int id, File file, UpdateType type, boolean announce) {
    this(plugin, id, file, type, null, announce);
  }

  /**
   * Initialize the updater with the provided callback.
   *
   * @param plugin The plugin that is checking for an update.
   * @param id The dev.bukkit.org id of the project.
   * @param file The file that the plugin is running from, get this by doing this.getFile() from
   * within your main class.
   * @param type Specify the type of update this will be. See {@link UpdateType}
   * @param callback The callback instance to notify when the updater has finished
   */
  public PluginUpdater(Plugin plugin, int id, File file, UpdateType type, UpdateCallback callback) {
    this(plugin, id, file, type, callback, false);
  }

  /**
   * Initialize the updater with the provided callback.
   *
   * @param plugin The plugin that is checking for an update.
   * @param id The dev.bukkit.org id of the project.
   * @param file The file that the plugin is running from, get this by doing this.getFile() from
   * within your main class.
   * @param type Specify the type of update this will be. See {@link UpdateType}
   * @param callback The callback instance to notify when the updater has finished
   * @param announce True if the program should announce the progress of new updates in console.
   */
  public PluginUpdater(Plugin plugin, int id, File file, UpdateType type, UpdateCallback callback,
      boolean announce) {
    this.plugin = plugin;
    this.type = type;
    this.announce = announce;
    this.file = file;
    this.id = id;
    this.updateFolder = this.plugin.getServer().getUpdateFolderFile();
    this.callback = callback;

    final File pluginFile = this.plugin.getDataFolder().getParentFile();
    final File updaterFile = new File(pluginFile, "updater");
    final File updaterConfigFile = new File(updaterFile, "bedwarsrel.yml");

    YamlConfiguration config = new YamlConfiguration();
    config.options().header(
        "This configuration file affects all plugins using the updater system (version 2+ - http://forums.bukkit.org/threads/96681/ )"
            + '\n'
            + "If you wish to use your API key, read http://wiki.bukkit.org/ServerMods_API and place it below."
            + '\n'
            + "Some updating systems will not adhere to the disabled value, but these may be turned off in their plugin's configuration.");
    config.addDefault(API_KEY_CONFIG_KEY, API_KEY_DEFAULT);
    config.addDefault(DISABLE_CONFIG_KEY, DISABLE_DEFAULT);

    if (!updaterFile.exists()) {
      this.fileIOOrError(updaterFile, updaterFile.mkdir(), true);
    }

    boolean createFile = !updaterConfigFile.exists();
    try {
      if (createFile) {
        this.fileIOOrError(updaterConfigFile, updaterConfigFile.createNewFile(), true);
        config.options().copyDefaults(true);
        config.save(updaterConfigFile);
      } else {
        config.load(updaterConfigFile);
      }
    } catch (final Exception e) {
      final String message;
      if (createFile) {
        message = "The updater could not create configuration at " + updaterFile.getAbsolutePath();
      } else {
        message = "The updater could not load configuration at " + updaterFile.getAbsolutePath();
      }
      this.plugin.getLogger().log(Level.SEVERE, message, e);
    }

    if (config.getBoolean(DISABLE_CONFIG_KEY)) {
      this.result = UpdateResult.DISABLED;
      return;
    }

    String key = config.getString(API_KEY_CONFIG_KEY);
    if (API_KEY_DEFAULT.equalsIgnoreCase(key) || "".equals(key)) {
      key = null;
    }

    this.apiKey = key;

    try {
      this.url = new URL(PluginUpdater.HOST + PluginUpdater.QUERY + this.id);
    } catch (final MalformedURLException e) {
      this.plugin.getLogger().log(Level.SEVERE,
          "The project ID provided for updating, " + this.id + " is invalid.", e);
      this.result = UpdateResult.FAIL_BADID;
    }

    if (this.result != UpdateResult.FAIL_BADID) {
      this.thread = new Thread(new UpdateRunnable());
      this.thread.start();
    } else {
      runUpdater();
    }
  }

  /**
   * Remove possibly leftover files from the update folder.
   */
  private void deleteOldFiles() {
    // Just a quick check to make sure we didn't leave any files from last
    // time...
    File[] list = listFilesOrError(this.updateFolder);
    for (final File xFile : list) {
      if (xFile.getName().endsWith(".zip")) {
        this.fileIOOrError(xFile, xFile.mkdir(), true);
      }
    }
  }

  /**
   * Download a file and save it to the specified folder.
   */
  private void downloadFile() {
    BufferedInputStream in = null;
    FileOutputStream fout = null;
    try {
      URL fileUrl = new URL(this.versionLink);
      final int fileLength = fileUrl.openConnection().getContentLength();
      in = new BufferedInputStream(fileUrl.openStream());
      fout = new FileOutputStream(new File(this.updateFolder, file.getName()));

      final byte[] data = new byte[PluginUpdater.BYTE_SIZE];
      int count;
      if (this.announce) {
        this.plugin.getLogger().info("About to download a new update: " + this.versionName);
      }
      long downloaded = 0;
      while ((count = in.read(data, 0, PluginUpdater.BYTE_SIZE)) != -1) {
        downloaded += count;
        fout.write(data, 0, count);
        final int percent = (int) ((downloaded * 100) / fileLength);
        if (this.announce && ((percent % 10) == 0)) {
          this.plugin.getLogger()
              .info("Downloading update: " + percent + "% of " + fileLength + " bytes.");
        }
      }
    } catch (Exception ex) {
      this.plugin.getLogger().log(Level.WARNING,
          "The auto-updater tried to download a new update, but was unsuccessful.", ex);
      this.result = PluginUpdater.UpdateResult.FAIL_DOWNLOAD;
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (final IOException ex) {
        this.plugin.getLogger().log(Level.SEVERE, null, ex);
      }
      try {
        if (fout != null) {
          fout.close();
        }
      } catch (final IOException ex) {
        this.plugin.getLogger().log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   * Perform a file operation and log any errors if it fails.
   *
   * @param file file operation is performed on.
   * @param result result of file operation.
   * @param create true if a file is being created, false if deleted.
   */
  private void fileIOOrError(File file, boolean result, boolean create) {
    if (!result) {
      this.plugin.getLogger().severe("The updater could not " + (create ? "create" : "delete")
          + " file at: " + file.getAbsolutePath());
    }
  }

  /**
   * Check for the version in the bedwars github repository
   *
   * @return version string
   */
  private String getCustomVersion() {
    String version = null;

    try {
      URL url =
          new URL("https://raw.githubusercontent.com/Yannici/bedwars-reloaded/master/release.txt");
      URLConnection connection = null;

      if (BedwarsRel.getInstance().isMineshafterPresent()) {
        connection = url.openConnection(Proxy.NO_PROXY);
      } else {
        connection = url.openConnection();
      }

      connection.setUseCaches(false);
      connection.setDoOutput(true);

      // request
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      ArrayList<String> versions = new ArrayList<String>();
      while ((line = reader.readLine()) != null) {
        versions.add(line);
      }
      if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_7")) {
        version = versions.get(0);
      } else {
        version = versions.get(1);
      }
      reader.close();

    } catch (Exception ex) {
      this.plugin.getLogger().severe("The updater could not check version!");
    }

    return version;
  }

  /**
   * Get the latest version's direct file link.
   *
   * @return latest version's file link.
   */
  public String getLatestFileLink() {
    this.waitForThread();
    return this.versionLink;
  }

  /**
   * Get the latest version's game version (such as "CB 1.2.5-R1.0").
   *
   * @return latest version's game version.
   */
  public String getLatestGameVersion() {
    this.waitForThread();
    return this.versionGameVersion;
  }

  /**
   * Get the latest version's name (such as "Project v1.0").
   *
   * @return latest version's name.
   */
  public String getLatestName() {
    this.waitForThread();
    return this.versionName;
  }

  /**
   * Get the latest version's release type.
   *
   * @return latest version's release type.
   * @see ReleaseType
   */
  public ReleaseType getLatestType() {
    this.waitForThread();
    if (this.versionType != null) {
      for (ReleaseType type : ReleaseType.values()) {
        if (this.versionType.equalsIgnoreCase(type.name())) {
          return type;
        }
      }
    }
    return null;
  }

  /**
   * Get the result of the update process.
   *
   * @return result of the update process.
   * @see UpdateResult
   */
  public PluginUpdater.UpdateResult getResult() {
    this.waitForThread();
    return this.result;
  }

  /**
   * Gets the latest version check result
   *
   * @return latest version check result
   */
  public boolean isLatestVersionCheck() {
    return this.lastVersionCheck;
  }

  private File[] listFilesOrError(File folder) {
    File[] contents = folder.listFiles();
    if (contents == null) {
      this.plugin.getLogger()
          .severe("The updater could not access files at: " + this.updateFolder.getAbsolutePath());
      return new File[0];
    } else {
      return contents;
    }
  }

  /**
   * Find any new files extracted from an update into the plugin's data directory.
   *
   * @param zipPath path of extracted files.
   */
  private void moveNewZipFiles(String zipPath) {
    File[] list = listFilesOrError(new File(zipPath));
    for (final File dFile : list) {
      if (dFile.isDirectory() && this.pluginExists(dFile.getName())) {
        // Current dir
        final File oFile = new File(this.plugin.getDataFolder().getParent(), dFile.getName());
        // List of existing files in the new dir
        final File[] dList = listFilesOrError(dFile);
        // List of existing files in the current dir
        final File[] oList = listFilesOrError(oFile);
        for (File cFile : dList) {
          // Loop through all the files in the new dir
          boolean found = false;
          for (final File xFile : oList) {
            // Loop through all the contents in the current dir to
            // see if it exists
            if (xFile.getName().equals(cFile.getName())) {
              found = true;
              break;
            }
          }
          if (!found) {
            // Move the new file into the current dir
            File output = new File(oFile, cFile.getName());
            this.fileIOOrError(output, cFile.renameTo(output), true);
          } else {
            // This file already exists, so we don't need it
            // anymore.
            this.fileIOOrError(cFile, cFile.delete(), false);
          }
        }
      }
      this.fileIOOrError(dFile, dFile.delete(), false);
    }
    File zip = new File(zipPath);
    this.fileIOOrError(zip, zip.delete(), false);
  }

  /**
   * Check if the name of a jar is one of the plugins currently installed, used for extracting the
   * correct files out of a zip.
   *
   * @param name a name to check for inside the plugins folder.
   * @return true if a file inside the plugins folder is named this.
   */
  private boolean pluginExists(String name) {
    File[] plugins = listFilesOrError(new File("plugins"));
    for (final File file : plugins) {
      if (file.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Make a connection to the BukkitDev API and request the newest file's details.
   *
   * @return true if successful.
   */
  private boolean read() {
    try {
      final URLConnection conn = this.url.openConnection();
      conn.setConnectTimeout(5000);

      if (this.apiKey != null) {
        conn.addRequestProperty("X-API-Key", this.apiKey);
      }
      conn.addRequestProperty("User-Agent", PluginUpdater.USER_AGENT);
      conn.setDoOutput(true);

      final BufferedReader reader =
          new BufferedReader(new InputStreamReader(conn.getInputStream()));
      final String response = reader.readLine();

      final JSONArray array = (JSONArray) JSONValue.parse(response);

      if (array.isEmpty()) {
        this.plugin.getLogger()
            .warning("The updater could not find any files for the project id " + this.id);
        this.result = UpdateResult.FAIL_BADID;
        return false;
      }

      JSONObject latestUpdate = (JSONObject) array.get(array.size() - 1);
      this.versionName = (String) latestUpdate.get(PluginUpdater.TITLE_VALUE);
      this.versionLink = (String) latestUpdate.get(PluginUpdater.LINK_VALUE);
      this.versionType = (String) latestUpdate.get(PluginUpdater.TYPE_VALUE);
      this.versionGameVersion = (String) latestUpdate.get(PluginUpdater.VERSION_VALUE);
      this.versionCustom = this.getCustomVersion();

      return true;
    } catch (final IOException e) {
      if (e.getMessage().contains("HTTP response code: 403")) {
        this.plugin.getLogger()
            .severe("dev.bukkit.org rejected the API key provided in plugins/updater/config.yml");
        this.plugin.getLogger()
            .severe("Please double-check your configuration to ensure it is correct.");
        this.result = UpdateResult.FAIL_APIKEY;
      } else {
        this.plugin.getLogger()
            .severe("The updater could not contact dev.bukkit.org for updating.");
        this.plugin.getLogger().severe(
            "If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
        this.result = UpdateResult.FAIL_DBO;
      }
      this.plugin.getLogger().log(Level.SEVERE, null, e);
      return false;
    }
  }

  private void runCallback() {
    this.callback.onFinish(this);
  }

  private void runUpdater() {
    if (this.url != null && (this.read() && this.versionCheck())) {
      this.lastVersionCheck = true;
      // Obtain the results of the project's file feed
      if ((this.versionLink != null) && (this.type != UpdateType.NO_DOWNLOAD)) {
        String name = this.file.getName();
        // If it's a zip file, it shouldn't be downloaded as the
        // plugin's name
        if (this.versionLink.endsWith(".zip")) {
          name = this.versionLink.substring(this.versionLink.lastIndexOf("/") + 1);
        }
        this.saveFile(name);
      } else {
        this.result = UpdateResult.UPDATE_AVAILABLE;
      }
    } else {
      this.result = UpdateResult.NO_UPDATE;
      this.lastVersionCheck = false;
    }

    if (this.callback != null) {
      new BukkitRunnable() {
        @Override
        public void run() {
          runCallback();
        }
      }.runTask(this.plugin);
    }
  }

  /**
   * Save an update from dev.bukkit.org into the server's update folder.
   *
   * @param file the name of the file to save it as.
   */
  private void saveFile(String file) {
    final File folder = this.updateFolder;

    deleteOldFiles();
    if (!folder.exists()) {
      this.fileIOOrError(folder, folder.mkdir(), true);
    }
    downloadFile();

    // Check to see if it's a zip file, if it is, unzip it.
    final File dFile = new File(folder.getAbsolutePath(), file);
    if (dFile.getName().endsWith(".zip")) {
      // Unzip
      this.unzip(dFile.getAbsolutePath());
    }
    if (this.announce) {
      this.plugin.getLogger().info("Finished updating.");
    }
  }

  /**
   * <b>If you wish to run mathematical versioning checks, edit this method.</b>
   * <p>
   * With default behavior, updater will NOT verify that a remote version available on BukkitDev
   * which is not this version is indeed an "update". If a version is present on BukkitDev that is
   * not the version that is currently running, updater will assume that it is a newer version. This
   * is because there is no standard versioning scheme, and creating a calculation that can
   * determine whether a new update is actually an update is sometimes extremely complicated.
   * </p>
   * <p>
   * updater will call this method from {@link #versionCheck()} before deciding whether the remote
   * version is actually an update. If you have a specific versioning scheme with which a
   * mathematical determination can be reliably made to decide whether one version is higher than
   * another, you may revise this method, using the local and remote version parameters, to execute
   * the appropriate check.
   * </p>
   * <p>
   * Returning a value of <b>false</b> will tell the update process that this is NOT a new version.
   * Without revision, this method will always consider a remote version at all different from that
   * of the local version a new update.
   * </p>
   *
   * @param localVersion the current version
   * @param remoteVersion the remote version
   * @return true if updater should consider the remote version an update, false if not.
   */
  public boolean shouldUpdate(String localVersion, String remoteVersion) {
    return !localVersion.equalsIgnoreCase(remoteVersion);
  }

  /**
   * Part of Zip-File-Extractor, modified by Gravity for use with updater.
   *
   * @param file the location of the file to extract.
   */
  private void unzip(String file) {
    final File fSourceZip = new File(file);
    try {
      final String zipPath = file.substring(0, file.length() - 4);
      ZipFile zipFile = new ZipFile(fSourceZip);
      Enumeration<? extends ZipEntry> e = zipFile.entries();
      while (e.hasMoreElements()) {
        ZipEntry entry = e.nextElement();
        File destinationFilePath = new File(zipPath, entry.getName());
        this.fileIOOrError(destinationFilePath.getParentFile(),
            destinationFilePath.getParentFile().mkdirs(), true);
        if (!entry.isDirectory()) {
          final BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
          int b;
          final byte[] buffer = new byte[PluginUpdater.BYTE_SIZE];
          final FileOutputStream fos = new FileOutputStream(destinationFilePath);
          final BufferedOutputStream bos = new BufferedOutputStream(fos, PluginUpdater.BYTE_SIZE);
          while ((b = bis.read(buffer, 0, PluginUpdater.BYTE_SIZE)) != -1) {
            bos.write(buffer, 0, b);
          }
          bos.flush();
          bos.close();
          bis.close();
          final String name = destinationFilePath.getName();
          if (name.endsWith(".jar") && this.pluginExists(name)) {
            File output = new File(this.updateFolder, name);
            this.fileIOOrError(output, destinationFilePath.renameTo(output), true);
          }
        }
      }
      zipFile.close();

      // Move any plugin data folders that were included to the right
      // place, Bukkit won't do this for us.
      moveNewZipFiles(zipPath);

    } catch (final IOException e) {
      this.plugin.getLogger().log(Level.SEVERE,
          "The auto-updater tried to unzip a new update file, but was unsuccessful.", e);
      this.result = PluginUpdater.UpdateResult.FAIL_DOWNLOAD;
    } finally {
      this.fileIOOrError(fSourceZip, fSourceZip.delete(), false);
    }
  }

  /**
   * Check to see if the program should continue by evaluating whether the plugin is already
   * updated, or shouldn't be updated.
   *
   * @return true if the version was located and is not the same as the remote's newest.
   */
  private boolean versionCheck() {
    String currentVersion = this.versionCustom;

    if (currentVersion == null) {
      return false;
    }

    String[] activeVersionSplit = this.plugin.getDescription().getVersion().split("\\.");
    String[] currentVersionSplit = currentVersion.split("\\.");

    try {
      int activeMasterVersion = Integer.valueOf(activeVersionSplit[0]);
      int currentMasterVersion = Integer.valueOf(currentVersionSplit[0]);
      if (currentMasterVersion > activeMasterVersion) {
        return true;
      } else if (activeMasterVersion > currentMasterVersion) {
        return false;
      }

      int activeMilestoneVersion = Integer.valueOf(activeVersionSplit[1]);
      int currentMilestoneVersion = Integer.valueOf(currentVersionSplit[1]);
      if (currentMilestoneVersion > activeMilestoneVersion) {
        return true;
      } else if (activeMilestoneVersion > currentMilestoneVersion) {
        return false;
      }

      int activeBuildVersion = Integer.valueOf(activeVersionSplit[2]);
      int currentBuildVersion = Integer.valueOf(currentVersionSplit[2]);
      if (currentBuildVersion > activeBuildVersion) {
        return true;
      } else if (activeBuildVersion > currentBuildVersion) {
        return false;
      }
    } catch (Exception ex) {
      // just error handling
    }

    return false;
  }

  /**
   * As the result of updater output depends on the thread's completion, it is necessary to wait for
   * the thread to finish before allowing anyone to check the result.
   */
  private void waitForThread() {
    if ((this.thread != null) && this.thread.isAlive()) {
      try {
        this.thread.join();
      } catch (final InterruptedException e) {
        this.plugin.getLogger().log(Level.SEVERE, null, e);
      }
    }
  }

  /**
   * Represents the various release types of a file on BukkitDev.
   */
  public enum ReleaseType {
    /**
     * An "alpha" file.
     */
    ALPHA,
    /**
     * A "beta" file.
     */
    BETA,
    /**
     * A "release" file.
     */
    RELEASE
  }

  /**
   * Gives the developer the result of the update process. Can be obtained by called
   * {@link #getResult()}
   */
  public enum UpdateResult {
    /**
     * The updater found an update, and has readied it to be loaded the next time the server
     * restarts/reloads.
     */
    SUCCESS,
    /**
     * The updater did not find an update, and nothing was downloaded.
     */
    NO_UPDATE,
    /**
     * The server administrator has disabled the updating system.
     */
    DISABLED,
    /**
     * The updater found an update, but was unable to download it.
     */
    FAIL_DOWNLOAD,
    /**
     * For some reason, the updater was unable to contact dev.bukkit.org to download the file.
     */
    FAIL_DBO,
    /**
     * When running the version check, the file on DBO did not contain a recognizable version.
     */
    FAIL_NOVERSION,
    /**
     * The id provided by the plugin running the updater was invalid and doesn't exist on DBO.
     */
    FAIL_BADID,
    /**
     * The server administrator has improperly configured their API key in the configuration.
     */
    FAIL_APIKEY,
    /**
     * The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it
     * wasn't downloaded.
     */
    UPDATE_AVAILABLE
  }

  /**
   * Allows the developer to specify the type of update that will be run.
   */
  public enum UpdateType {
    /**
     * Run a version check, and then if the file is out of date, download the newest version.
     */
    DEFAULT,
    /**
     * Don't run a version check, just find the latest update and download it.
     */
    NO_VERSION_CHECK,
    /**
     * Get information about the version and the download size, but don't actually download
     * anything.
     */
    NO_DOWNLOAD
  }

  /**
   * Called on main thread when the updater has finished working, regardless of result.
   */
  public interface UpdateCallback {

    /**
     * Called when the updater has finished working.
     *
     * @param updater The updater instance
     */
    void onFinish(PluginUpdater updater);
  }

  private class UpdateRunnable implements Runnable {

    @Override
    public void run() {
      runUpdater();
    }
  }
}
