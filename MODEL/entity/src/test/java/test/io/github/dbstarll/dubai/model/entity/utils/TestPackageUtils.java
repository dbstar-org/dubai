package test.io.github.dbstarll.dubai.model.entity.utils;

import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.entity.test.o2.Package;
import io.github.dbstarll.dubai.model.entity.test.o2.PublicPackageInterfaceEntity;
import io.github.dbstarll.dubai.model.entity.test.o3.NoPackageInterfaceEntity;
import io.github.dbstarll.dubai.model.entity.test.o4.ClassPackageInterfaceEntity;
import io.github.dbstarll.dubai.model.entity.utils.PackageUtils;
import junit.framework.TestCase;

import static org.junit.Assert.assertThrows;

public class TestPackageUtils extends TestCase {
    /**
     * 测试创建实例.
     */
    public void testNewInstance() {
        assertThrows(IllegalAccessException.class, () -> PackageUtils.class.newInstance());
    }

    /**
     * 测试getPackageInterface方法.
     */
    public void testGetPackageInterface() {
        assertEquals("io.github.dbstarll.dubai.model.entity.test.Package",
                PackageUtils.getPackageInterface(InterfaceEntity.class, null).getName());
        assertEquals("io.github.dbstarll.dubai.model.entity.test.o2.Package",
                PackageUtils.getPackageInterface(ClassPackageInterfaceEntity.class, Package.class).getName());
        assertEquals("io.github.dbstarll.dubai.model.entity.test.o2.Package",
                PackageUtils.getPackageInterface(PublicPackageInterfaceEntity.class, Package.class).getName());
        assertEquals("io.github.dbstarll.dubai.model.entity.test.o2.Package",
                PackageUtils.getPackageInterface(NoPackageInterfaceEntity.class, Package.class).getName());
    }
}
