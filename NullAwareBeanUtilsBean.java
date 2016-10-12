import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapted from
 * http://stackoverflow.com/questions/24371110/reflection-copy-non-null-properties-from-one-object-to-another-beanutils
 */
public class NullAwareBeanUtilsBean extends BeanUtilsBean {

    private static final Logger log = LoggerFactory.getLogger(NullAwareBeanUtilsBean.class);

    @SuppressWarnings("ConstantConditions")
    @Override
    public void copyProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {
        try {
            Object childObj = getPropertyUtils().getSimpleProperty(bean, name);
            Class fieldClass = PropertyUtils.getPropertyType(bean, name);

            //log.debug("copyProperty begin->bean={}, name={}, childObj={}, fieldClass={}, value={}", bean, name, childObj, fieldClass, value);

            if (ClassUtils.isPrimitiveOrWrapper(fieldClass) || fieldClass.isAssignableFrom(String.class)) {
                //log.debug("PRIMITIVE");
                childObj = value;
            } else if (fieldClass.isAssignableFrom(Date.class)) {
                //log.debug("DATE");
                if (value != null) {
                    // Assuming date comes in epoch long value
                    childObj = new Date((Long) value);
                }
            } else if (fieldClass.isEnum()) {
                //log.debug("ENUM");
                if (value != null) {
                    childObj = Enum.valueOf(fieldClass, value.toString());
                } else {
                    childObj = null;
                }
            } else {
                //log.debug("NON-PRIMITIVE");
                if (fieldClass == BigDecimal.class) {
                    if (value != null) {
                        childObj = new BigDecimal(value.toString());
                    }
                }
                //Note: Collection logic currently only works with simple primitives (not collections of complex, custom objects).
                else if (fieldClass == Set.class) {
                    //log.debug("SET");
                    if (value != null) {
                        childObj = new LinkedHashSet<Object>((List) value);
                    } else {
                        childObj = new LinkedHashSet<Object>();
                    }
                } else {
                    //log.debug("Complex object, performing recursion");
                    if (childObj != null) {
                        copyProperties(childObj, value);
                    }
                }
            }
            //log.debug("copyProperty end->bean={}, name={}, childObj={}, value={} ", bean, name, childObj, value);
            super.copyProperty(bean, name, childObj);
        } catch (NoSuchMethodException e) {
            log.warn("NoSuchMethod Exception: " + e);
        }
    }
}
