package com.cfams.campus;

/** 离线检查使用的字段注入工具，不参与生产代码。 */
public final class TestInjection {
    /** 禁止实例化测试工具。 */
    private TestInjection() {
    }

    /** 按字段名填充测试替身，复现Spring字段注入而无需启动服务。 */
    public static <T> T inject(T bean, Object... fields) {
        if (fields.length % 2 != 0) {
            throw new IllegalArgumentException("字段名和值必须成对");
        }
        for (int index = 0; index < fields.length; index += 2) {
            try {
                java.lang.reflect.Field field = bean.getClass().getDeclaredField((String) fields[index]);
                field.setAccessible(true);
                field.set(bean, fields[index + 1]);
            } catch (ReflectiveOperationException error) {
                throw new AssertionError("测试依赖注入失败：" + fields[index], error);
            }
        }
        return bean;
    }
}
