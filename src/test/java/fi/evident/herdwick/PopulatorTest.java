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

    private final Database db = TestDatabaseProvider.databaseForProperties("hsqldb-connection.properties");

    private final Populator populator = new Populator(db);

    @Rule
    public final TransactionalTests transactionalTests = new TransactionalTests(db);

    @Test
    public void populateSimpleTable() {
        db.update("drop table if exists foo");
        db.update("create table foo (name varchar(10) primary key)");

        assertThat(populator.populate("foo", 50), is(50));

        assertThat(count("foo"), is(50));
    }

    @Test
    public void populateTableWithMultipleRequiredColumns() {
        db.update("drop table if exists foo");
        db.update("create table foo (name varchar(10) primary key, description varchar(20) not null, counter int not null)");

        populator.populate("foo", 50);

        assertThat(count("foo"), is(50));
    }

    @Test
    public void populateTableWithAutomaticPrimaryKey() {
        db.update("drop table if exists foo");
        db.update("create table foo (id serial primary key, name varchar(10) not null unique)");

        populator.populate("foo", 50);

        assertThat(count("foo"), is(50));
    }

    @Test
    public void booleanColumns() {
        db.update("drop table if exists foo");
        db.update("create table foo (id serial primary key, flag boolean not null)");

        populator.populate("foo", 50);

        assertThat(count("foo"), is(50));
    }

    @Test
    public void populateForeignKeys() {
        db.update("drop table if exists emp");
        db.update("drop table if exists dept");
        db.update("create table dept (id serial primary key, name varchar(10) not null)");
        db.update("create table emp (id serial primary key, name varchar(10) not null, dept_id int references dept not null)");

        populator.populate("dept", 10);
        populator.populate("emp", 100);

        assertThat(count("dept"), is(10));
        assertThat(count("emp"), is(100));
    }

    @Test
    public void multiColumnUnique() {
        db.update("drop table if exists foo");
        db.update("create table foo (id serial primary key, flag boolean not null, num varchar(10), unique (flag, num))");

        populator.populate("foo", 50);

        assertThat(count("foo"), is(50));
    }

    @Test
    public void multipleForeignKeys() {
        db.update("drop table if exists user_account_group");
        db.update("drop table if exists user_account");
        db.update("drop table if exists user_group");

        db.update("create table user_group (id serial primary key, name varchar(10) not null)");
        db.update("create table user_account (id serial primary key, name varchar(10) not null)");
        db.update("create table user_account_group (account_id int references user_account, group_id int references user_group, primary key (account_id, group_id))");

        populator.populate("user_account", 10);
        populator.populate("user_group", 10);
        populator.populate("user_account_group", 50);

        assertThat(count("user_account"), is(10));
        assertThat(count("user_group"), is(10));
        assertThat(count("user_account_group"), is(50));
    }

    @Test
    public void failingToCreateWholeBatch() {
        db.update("drop table if exists table_with_only_two_possible_rows");
        db.update("create table table_with_only_two_possible_rows (flag boolean primary key)");

        assertThat(populator.populate("table_with_only_two_possible_rows", 3), is(2));

        assertThat(count("table_with_only_two_possible_rows"), is(2));
    }

    @Test
    public void multiColumnKeys() {
        db.update("drop table if exists child");
        db.update("drop table if exists parent");

        db.update("create table parent (x int, y int, primary key (x,y))");
        db.update("create table child (id serial primary key, parent_x int, parent_y int, foreign key (parent_x,parent_y) references parent(x,y))");

        populator.populate("parent", 10);
        populator.populate("child", 20);

        assertThat(count("child"), is(20));
    }

    @Test
    public void takeExistingDataIntoAccountWhenCheckingUniqueConstraints() {
        db.update("drop table if exists foo");
        db.update("create table foo (id serial primary key, flag boolean not null unique)");

        db.update("insert into foo (flag) values (false)");

        assertThat(populator.populate("foo", 10), is(1));

        assertThat(count("foo"), is(2));
    }

    @Test
    public void batchModeFlag() {
        assertThat(populator.isBatchMode(), is(true));
        populator.setBatchMode(false);
        assertThat(populator.isBatchMode(), is(false));
    }

    private int count(String table) {
        return db.findUniqueInt("select count(*) from " + table);
    }
}
