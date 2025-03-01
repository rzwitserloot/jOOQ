/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: https://www.jooq.org/legal/licensing
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package org.jooq.jpa.extensions;

import java.lang.reflect.Method;

import org.jooq.ConverterContext;
import org.jooq.exception.MappingException;
import org.jooq.impl.AbstractContextConverter;
import org.jooq.tools.JooqLogger;
import org.jooq.tools.reflect.Reflect;

import jakarta.persistence.AttributeConverter;


/**
 * A converter that delegates data type conversions to a JPA
 * {@link AttributeConverter}.
 * <p>
 * This is particularly useful when generating code from a
 * <code>JPADatabase</code>, which reverse engineers JPA annotated entities, in
 * case of which, by default, the {@link AttributeConverter} annotations are
 * discovered automatically and the user-defined type is applied also in the
 * jOOQ meta model.
 *
 * @author Lukas Eder
 */
public final class JPAConverter<T, U> extends AbstractContextConverter<T, U> {

    private static final JooqLogger        log = JooqLogger.getLogger(JPAConverter.class);

    private final AttributeConverter<U, T> delegate;

    public JPAConverter(Class<? extends AttributeConverter<U, T>> klass) {
        super(fromType(klass), toType(klass));

        try {
            this.delegate = Reflect.on(klass).create().get();
        }
        catch (Exception e) {
            throw new MappingException("Cannot instanciate AttributeConverter", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static final <T> Class<T> fromType(Class<? extends AttributeConverter<?, T>> klass) {
        Method candidate = null;

        // [#10951] Try finding the bridge method, skip the inherited method if applicable
        for (Method method : klass.getDeclaredMethods())
            if ("convertToDatabaseColumn".equals(method.getName()))
                if (method.getReturnType() == Object.class)
                    candidate = method;
                else
                    return (Class<T>) method.getReturnType();

        if (candidate != null) {
            log.warn("Couldn't find bridge method to detect generic type bound for " + klass.getName() + "::convertToDatabaseColumn");
            return (Class<T>) candidate.getReturnType();
        }

        throw new IllegalArgumentException();
    }

    @SuppressWarnings("unchecked")
    private static final <U> Class<U> toType(Class<? extends AttributeConverter<U, ?>> klass) {
        Method candidate = null;

        // [#10951] Try finding the bridge method, skip the inherited method if applicable
        for (Method method : klass.getDeclaredMethods())
            if ("convertToEntityAttribute".equals(method.getName()))
                if (method.getReturnType() == Object.class)
                    candidate = method;
                else
                    return (Class<U>) method.getReturnType();

        if (candidate != null) {
            log.warn("Couldn't find bridge method to detect generic type bound for " + klass.getName() + "::convertToEntityAttribute");
            return (Class<U>) candidate.getReturnType();
        }

        throw new IllegalArgumentException();
    }

    @Override
    public final U from(T t, ConverterContext scope) {
        return delegate.convertToEntityAttribute(t);
    }

    @Override
    public final T to(U u, ConverterContext scope) {
        return delegate.convertToDatabaseColumn(u);
    }
}
