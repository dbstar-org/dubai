package io.github.dbstarll.dubai.model.entity.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PackageUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageUtils.class);

    private static final ConcurrentMap<String, Class<?>> PACKAGES = new ConcurrentHashMap<>();

    /**
     * 根据指定的类，寻找用作代理包路径的Package.class.
     *
     * @param entityClass             在指定的类所在的包下寻找
     * @param defaultPackageInterface 若未找到对应的类，返回此缺省类
     * @return 可用作代理包路径的Package.class
     */
    public static <E> Class<?> getPackageInterface(Class<E> entityClass, Class<?> defaultPackageInterface) {
        final String packageName = entityClass.getPackage().getName();
        if (!PACKAGES.containsKey(packageName)) {
            final Class<?> packageInterface = loadPackageInterface(entityClass.getClassLoader(), packageName + ".Package",
                    defaultPackageInterface);
            LOGGER.info("loadPackageInterface for: {} with: {}", packageName, packageInterface);
            PACKAGES.putIfAbsent(packageName, packageInterface);
        }
        return PACKAGES.get(packageName);
    }

    private static <E> Class<?> loadPackageInterface(ClassLoader classLoader, String packageInterfaceName,
                                                     Class<?> defaultPackageInterface) {
        try {
            final Class<?> packageInterface = classLoader.loadClass(packageInterfaceName);
            if (packageInterface.isInterface()) {
                if (!Modifier.isPublic(packageInterface.getModifiers())) {
                    return packageInterface;
                } else {
                    LOGGER.warn("Package Not package scope: {}", packageInterface);
                }
            } else {
                LOGGER.warn("Package Not Interface: {}", packageInterface);
            }
        } catch (ClassNotFoundException ex) {
            LOGGER.warn("No Package Found for: {}", packageInterfaceName);
        }

        return defaultPackageInterface;
    }
}
