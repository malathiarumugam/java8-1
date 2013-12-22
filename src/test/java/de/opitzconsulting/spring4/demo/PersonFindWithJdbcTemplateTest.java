package de.opitzconsulting.spring4.demo;

import de.opitzconsulting.spring4.demo.domain.Person;
import de.opitzconsulting.spring4.demo.repository.PersonRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationConfig.class)
public class PersonFindWithJdbcTemplateTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL = "Select id, first_name, last_name from person where id = ?";

    private Person person;

    @Before
    public void doBefore() {
        // insert a demo person
        this.person = personRepository.saveAndFlush(new Person("Fred", "Feuerstein"));
        assertThat(person.getId(), notNullValue());
    }

    @Test
    public void findPersonWithInlineLambda() {
        List<Person> personList = jdbcTemplate.query(SQL,
                (rs, rowNum) -> new Person(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")), person.getId());

        assertThat(personList.size(), equalTo(1));
        assertThat(personList.get(0), equalTo(person));
    }

    @Test
    public void findPersonWithInlineLambdaAndPreparedStatementSetter() {
        List<Person> personList = jdbcTemplate.query(SQL,
                (ps) -> {
                    ps.setLong(1, person.getId());
                },
                (rs, rowNum) -> new Person(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")));

        assertThat(personList.size(), equalTo(1));
        assertThat(personList.get(0), equalTo(person));
    }

    @Test
    public void findPersonWithExplicitLambda() {
        List<Person> personList = jdbcTemplate.query(SQL,
                (ps) -> {
                    ps.setLong(1, person.getId());
                },
                (rs, rowNum) -> {
                    return new Person(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"));
                });

        assertThat(personList.size(), equalTo(1));
        assertThat(personList.get(0), equalTo(person));
    }

    @Test
    public void findPersonWithShortMethodReference() {
        jdbcTemplate.query(SQL, ps -> ps.setLong(1, person.getId()), this::mapPerson);
    }

    // RowMapper
    private Person mapPerson(ResultSet rs, int rowNum) throws SQLException {
        return new Person(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"));
    }
}
