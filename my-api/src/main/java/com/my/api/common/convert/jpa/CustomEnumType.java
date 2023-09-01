package com.my.api.common.convert.jpa;

import com.my.api.common.annotation.Description;
import com.my.api.common.model.BaseEnum;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SqlTypes;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

@Description(value = "BaseEnum 컨버터", comment = "BaseEnum <-> DB value 컨버팅")
public class CustomEnumType implements UserType<BaseEnum>, DynamicParameterizedType {

    private Class enumClass;

    @Override
    public void setParameterValues(Properties parameters) {
        ParameterType params = (ParameterType) parameters.get(PARAMETER_TYPE);
        enumClass = params.getReturnedClass();
    }

    @Override
    public int getSqlType() {
        return SqlTypes.VARCHAR;
    }

    @Override
    public Class<BaseEnum> returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(BaseEnum x, BaseEnum y) {
        return x == y;
    }

    @Override
    public int hashCode(BaseEnum x) {
        return x.hashCode();
    }

    @Override
    public BaseEnum nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        Object label = rs.getObject(position);
        return rs.wasNull() ? null : BaseEnum.getEnum(returnedClass(), label);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, BaseEnum value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, SqlTypes.VARCHAR);
        } else {
            st.setObject(index, value.getCode());
        }
    }

    @Override
    public BaseEnum deepCopy(BaseEnum value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(BaseEnum value) {
        return value;
    }

    @Override
    public BaseEnum assemble(Serializable cached, Object owner) {
        return (BaseEnum) cached;
    }
}
