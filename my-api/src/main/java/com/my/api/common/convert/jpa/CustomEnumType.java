package com.my.api.common.convert.jpa;

import com.my.api.common.annotation.Description;
import com.my.api.common.model.BaseEnum;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

@Description(value = "BaseEnum 컨버터", comment = "BaseEnum <-> DB value 컨버팅")
public class CustomEnumType implements UserType, DynamicParameterizedType {

    // 타입 명
    public static final String NAME = "com.brian.api.common.convert.jpa.CustomEnumType";

    private Class<? extends BaseEnum> enumClass;
    private String entityName;
    private String propertyName;

    @Override
    public void setParameterValues(Properties parameters) {
        ParameterType params = (ParameterType) parameters.get(PARAMETER_TYPE);

        enumClass = params.getReturnedClass();
        entityName = (String) parameters.get(ENTITY);
        propertyName = (String) parameters.get(PROPERTY);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        Object label = rs.getObject(names[0]);

        if (label == null) return null;

        // DB -> Enum
        return BaseEnum.getEnum(enumClass, label);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        // Enum -> DB
        if (value != null) {
            value = ((BaseEnum) value).getCode();
        }

        st.setObject(index, value);
    }

    @Override
    public Class returnedClass() {
        return enumClass;
    }

    @Override
    public int[] sqlTypes() {
        // 어디에 쓰이는 것일까..
        return new int[] { Types.VARCHAR };
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
