/*
 * Copyright (c) 2012 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.evident.herdwick;

import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.junit.TestDatabaseProvider;
import fi.evident.dalesbred.junit.TransactionalTests;
import org.junit.Rule;
import org.junit.Test;

public class PopulatorUnsuccessfulTest {


    private final Database db = TestDatabaseProvider.databaseForProperties("hsqldb-connection.properties");

    private final Populator populator = new Populator(db);

    @Rule
    public final TransactionalTests transactionalTests = new TransactionalTests(db);

    @Test(expected = RuntimeException.class)
    public void populateEmployeesWithoutDepartments() {
        db.update("drop table if exists emp");
        db.update("drop table if exists dept");
        db.update("create table dept (id serial primary key, name varchar(10) not null)");
        db.update("create table emp (id serial primary key, name varchar(10) not null, dept_id int references dept not null)");

        populator.populate("emp", 1);
    }
}
