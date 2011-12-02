package org.apache.ibatis.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Permission;
import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.migration.commands.NewCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MigratorTest {

  public static final String BLOG_PROPERTIES = "databases/blog/blog-derby.properties";

  private static PrintStream out;
  private static StringOutputStream buffer;

  @BeforeClass
  public static void setup() throws Exception {
    out = System.out;
    buffer = new StringOutputStream();
    System.setOut(new PrintStream(buffer));

    DataSource ds = createUnpooledDataSource(BLOG_PROPERTIES);
    Connection conn = ds.getConnection();
    SqlRunner executor = new SqlRunner(conn);
    safeRun(executor, "DROP TABLE bootstrap");
    safeRun(executor, "DROP TABLE comment");
    safeRun(executor, "DROP TABLE post_tag");
    safeRun(executor, "DROP TABLE tag");
    safeRun(executor, "DROP TABLE post");
    safeRun(executor, "DROP TABLE blog");
    safeRun(executor, "DROP TABLE author");
    safeRun(executor, "DROP PROCEDURE selectTwoSetsOfAuthors");
    safeRun(executor, "DROP PROCEDURE insertAuthor");
    safeRun(executor, "DROP PROCEDURE selectAuthorViaOutParams");
    safeRun(executor, "DROP TABLE changelog");
    conn.commit();
    conn.close();

    System.setSecurityManager(new SecurityManager() {

      @Override
      public void checkPermission(Permission perm) {
      }

      @Override
      public void checkPermission(Permission perm, Object context) {
      }

      @Override
      public void checkExit(int status) {
        throw new RuntimeException("System exited with error code: " + status);
      }
    });
  }

  @AfterClass
  public static void teardown() {
    System.setOut(out);
    System.setSecurityManager(null);
  }

  @Test
  public void shouldRunThroughFullMigrationUseCaseInOneTestToEnsureOrder() throws Throwable {
    try {
      File f = getExampleDir();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "--env=development", "bootstrap"));
      assertTrue(buffer.toString().contains("--  Bootstrap.sql"));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "sta"));
      assertTrue(buffer.toString().contains("...pending..."));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "up", "3000"));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "status"));
      assertFalse(buffer.toString().contains("...pending..."));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "down", "2"));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "status"));
      assertTrue(buffer.toString().contains("...pending..."));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "version", "20080827200215"));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "status"));
      assertFalse(buffer.toString().contains("...pending..."));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "down"));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "status"));
      assertTrue(buffer.toString().contains("...pending..."));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "pending"));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "status"));
      assertFalse(buffer.toString().contains("...pending..."));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "--help"));
      assertTrue(buffer.toString().contains("--help"));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "script", "20080827200212", "20080827200214"));
      assertFalse(buffer.toString().contains("20080827200210"));
      assertFalse(buffer.toString().contains("20080827200211"));
      assertTrue(buffer.toString().contains("20080827200212"));
      assertTrue(buffer.toString().contains("20080827200213"));
      assertTrue(buffer.toString().contains("20080827200214"));
      assertFalse(buffer.toString().contains("20080827200215"));
      assertFalse(buffer.toString().contains("-- @UNDO"));
      buffer.clear();

      safeMigratorMain(args("--path=" + f.getAbsolutePath(), "script", "20080827200215", "20080827200213"));
      assertFalse(buffer.toString().contains("20080827200210"));
      assertFalse(buffer.toString().contains("20080827200211"));
      assertFalse(buffer.toString().contains("20080827200212"));
      assertTrue(buffer.toString().contains("20080827200213"));
      assertTrue(buffer.toString().contains("20080827200214"));
      assertTrue(buffer.toString().contains("20080827200215"));
      assertTrue(buffer.toString().contains("-- @UNDO"));
      buffer.clear();
    } catch (Throwable t) {
      System.err.println(buffer);
      throw t;
    }
  }

  private void safeMigratorMain(String[] args) throws Exception {
    // Handles System.exit(1) calls so that the JVM doesn't terminate during unit tests.
    // See security manager in setup method.
    try {
      Migrator.main(args);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void shouldInitTempDirectory() throws Exception {
    File basePath = getTempDir();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath(), "scripts");
    assertEquals(3, scriptPath.list().length);
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "new", "test new migration"));
    assertEquals(4, scriptPath.list().length);

  }

  @Test
  public void useCustomTemplate() throws Exception {
    File basePath = getTempDir();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath(), "scripts");
    assertEquals(3, scriptPath.list().length);

    File templatePath = File.createTempFile("customTemplate","sql");
    templatePath.createNewFile();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "new", "test new migration", "--template=" + templatePath.getAbsolutePath()));
    assertEquals(4, scriptPath.list().length);

    templatePath.delete();
  }

  @Test
  public void useCustomTemplateWithNoValue() throws Exception {
    File basePath = getTempDir();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath(), "scripts");
    assertEquals(3, scriptPath.list().length);

    File templatePath = File.createTempFile("customTemplate", "sql");
    templatePath.createNewFile();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "new", "test new migration", "--template="));
    assertEquals(4, scriptPath.list().length);

    templatePath.delete();
  }

  @Test
  public void useCustomTemplateWithBadPath() throws Exception {
    System.setProperty("migrationHome", "/tmp");
    File basePath = getTempDir();
    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "init"));
    assertNotNull(basePath.list());
    assertEquals(4, basePath.list().length);
    File scriptPath = new File(basePath.getCanonicalPath(), "scripts");
    assertEquals(3, scriptPath.list().length);

    safeMigratorMain(args("--path=" + basePath.getAbsolutePath(), "new", "test new migration"));
    assertEquals(4, scriptPath.list().length);

    assertTrue(buffer.toString().contains("Your migrations configuration did not find your custom template.  Using the default template."));
  }

  private String[] args(String... args) {
    return args;
  }

  private File getExampleDir() throws IOException, URISyntaxException {
    URL resourceURL = Resources.getResourceURL(getClass().getClassLoader(), "org/apache/ibatis/migration/example/");
    File f = new File(resourceURL.toURI());
    assertTrue(f.exists());
    assertTrue(f.isDirectory());
    return f;
  }

  private File getTempDir() throws IOException {
    File f = File.createTempFile("migration", "test");
    assertTrue(f.delete());
    assertTrue(f.mkdir());
    assertTrue(f.exists());
    assertTrue(f.isDirectory());
    f.deleteOnExit();
    return f;
  }

  private static class StringOutputStream extends OutputStream {

    private StringBuilder builder = new StringBuilder();

    public void write(int b) throws IOException {
      builder.append((char) b);
//      out.write(b);
    }

    @Override
    public String toString() {
      return builder.toString();
    }

    public void clear() {
      builder.setLength(0);
    }
  }

  private static void safeRun(SqlRunner executor, String sql) {
    try {
      executor.run(sql);
    } catch (Exception e) {
      //ignore
    }
  }

  public static UnpooledDataSource createUnpooledDataSource(String resource) throws IOException {
    Properties props = Resources.getResourceAsProperties(resource);
    UnpooledDataSource ds = new UnpooledDataSource();
    ds.setDriver(props.getProperty("driver"));
    ds.setUrl(props.getProperty("url"));
    ds.setUsername(props.getProperty("username"));
    ds.setPassword(props.getProperty("password"));
    return ds;
  }


}