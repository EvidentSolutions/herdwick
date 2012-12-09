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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PopulatorTest {

    private final Database db = TestDatabaseProvider.databaseForProperties("postgresql-connection.properties");

    @Rule
    public final TransactionalTests transactionalTests = new TransactionalTests(db);

    @Test
    public void populateSimpleTable() {
        db.update("drop table if exists foo");
        db.update("create table foo (name varchar(10) primary key)");

        Populator populator = new Populator(db);
        populator.populate("foo", 50);

        assertThat(db.findUniqueInt("select count(*) from foo"), is(50));
    }

    @Test
    public void populateTableWithMultipleRequiredColumns() {
        db.update("drop table if exists foo");
        db.update("create table foo (name varchar(10) primary key, description varchar(20) not null, counter int not null)");

        Populator populator = new Populator(db);
        populator.populate("foo", 50);

        assertThat(db.findUniqueInt("select count(*) from foo"), is(50));
    }

    @Test
    public void populateTableWithAutomaticPrimaryKey() {
        db.update("drop table if exists foo");
        db.update("create table foo (id serial primary key, name varchar(10) not null unique)");

        Populator populator = new Populator(db);
        populator.populate("foo", 50);

        assertThat(db.findUniqueInt("select count(*) from foo"), is(50));
    }

    @Test
    public void booleanColumns() {
        db.update("drop table if exists foo");
        db.update("create table foo (id serial primary key, flag boolean not null)");

        Populator populator = new Populator(db);
        populator.populate("foo", 50);

        assertThat(db.findUniqueInt("select count(*) from foo"), is(50));
    }

    @Test
    public void populateForeignKeys() {
        db.update("drop table if exists emp");
        db.update("drop table if exists dept");
        db.update("create table dept (id serial primary key, name varchar(10) not null)");
        db.update("create table emp (id serial primary key, name varchar(10) not null, dept_id int references dept not null)");

        Populator populator = new Populator(db);
        populator.populate("dept", 10);
        populator.populate("emp", 100);

        assertThat(db.findUniqueInt("select count(*) from dept"), is(10));
        assertThat(db.findUniqueInt("select count(*) from emp"), is(100));
    }

    @Test
    public void multiColumnUnique() {
        db.update("drop table if exists foo");
        db.update("create table foo (id serial primary key, flag boolean not null, num varchar(10), unique (flag, num))");

        Populator populator = new Populator(db);
        populator.populate("foo", 50);

        assertThat(db.findUniqueInt("select count(*) from foo"), is(50));
    }

    @Test
    public void multipleForeignKeys() {
        db.update("drop table if exists user_account_group");
        db.update("drop table if exists user_account");
        db.update("drop table if exists user_group");

        db.update("create table user_group (id serial primary key, name varchar(10) not null)");
        db.update("create table user_account (id serial primary key, name varchar(10) not null)");
        db.update("create table user_account_group (account_id int references user_account, group_id int references user_group, primary key (account_id, group_id))");

        Populator populator = new Populator(db);
        populator.populate("user_account", 10);
        populator.populate("user_group", 10);
        populator.populate("user_account_group", 50);

        assertThat(db.findUniqueInt("select count(*) from user_account"), is(10));
        assertThat(db.findUniqueInt("select count(*) from user_group"), is(10));
        assertThat(db.findUniqueInt("select count(*) from user_account_group"), is(50));
    }
}
