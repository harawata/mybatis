/*
 *    Copyright 2010 The myBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.guice;

import org.mybatis.guice.mappers.MapperProvider;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.util.Providers;

/**
 * 
 *
 * @version $Id$
 */
final class EachMapper extends AbstractBinderEach<Class<?>> {

    public EachMapper(Binder binder) {
        super(binder);
    }

    /**
     * {@inheritDoc}
     */
    public void doHandle(Class<?> mapperType) {
        this.bindMapper(mapperType);
    }

    private <T> void bindMapper(Class<T> mapperType) {
        this.getBinder().bind(mapperType).toProvider(Providers.guicify(new MapperProvider<T>(mapperType))).in(Scopes.SINGLETON);
    }

}
