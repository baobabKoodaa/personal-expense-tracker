package baobab;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonalExpenseTrackerApplicationTests {

	@Autowired
	private WebApplicationContext webAppContext;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
	}

	@Test
	public void signupAddsDataToDatabase() throws Throwable {
		//mockMvc.perform(post("/form").param("name", "Testname").param("address", "Testaddress")).andReturn();
		//assertEquals(1L, signupRepository.findAll().stream().filter(s -> s.getName().equals("Testname") && s.getAddress().equals("Testaddress")).count());
	}
}