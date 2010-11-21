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
package org.mybatis.guice.iterables;

/**
 * 
 *
 * @version $Id$
 */
public final class Do<T> {

    private final Iterable<T> iterable;

    protected Do(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    public void handle(Each<T> each) {
        if (each == null) {
            throw new IllegalArgumentException("Parameter 'each' must be not null");
        }

        for (T t : this.iterable) {
            each.doHandle(t);
        }
    }

}
