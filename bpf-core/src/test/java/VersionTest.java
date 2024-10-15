import com.github.alantr7.bukkitplugin.versions.Version;
import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

    @Test
    public void testVersions() {
        Version version1 = Version.from("1.0.0");
        Version version2 = Version.from("1.0.1");
        Version version3 = Version.from("hello");
        Version version4 = Version.from("1.0.0.0");

        Version version5 = Version.from("1.0.0a");
        Version version6 = Version.from("1.0.0b");

        Assert.assertFalse(version1.isNewerThan(version2));
        Assert.assertTrue(version2.isNewerThan(version1));
        Assert.assertFalse(version3.isValid());
        Assert.assertEquals(version1, version4);
        Assert.assertFalse(version5.equals(version1));
        Assert.assertFalse(version5.equals(version6));
        Assert.assertTrue(version6.isNewerThan(version5));
        Assert.assertTrue(version1.isNewerThan(version5));
        Assert.assertTrue(version1.isNewerThan(version6));
    }

}
