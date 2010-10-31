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
package org.mybatis.spring.sample.mapper;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * This DAO is injected with a SqlSessionFactory that is used to get a SqlSession and call MyBatis API.
 *
 * @version $Id: UserMapperSqlSessionImpl.java 2658 2010-10-09 22:03:48Z eduardo.macarron $
 */
public class UserMapperSqlSessionImpl implements UserMapper {

    private SqlSessionFactory sqlSessionFactory;

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public User getUser(String userId) {
        SqlSession session = sqlSessionFactory.openSession();
        try {
          return (User) session.selectOne("org.mybatis.spring.sample.mapper.UserMapper.getUser", userId);
        } finally {
          session.close();
        }   
    }
}
