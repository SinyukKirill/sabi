package de.bluewhale.sabi;

import de.bluewhale.sabi.persistence.UserDAOTest;
import de.bluewhale.sabi.services.TankServiceTest;
import de.bluewhale.sabi.services.UserServiceTest;
import de.bluewhale.sabi.util.EncryptionServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite to test them all.
 *
 * @author schubert
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({UserDAOTest.class,
                     UserServiceTest.class,
                     EncryptionServiceTest.class,
                     TankServiceTest.class})
public class TestSuite {
}