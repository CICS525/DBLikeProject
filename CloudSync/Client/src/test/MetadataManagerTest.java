package test;

import static org.junit.Assert.*;

import cloudsync.client.MetadataManager;
import cloudsync.sharedInterface.Metadata;

import org.junit.Test;

/**
 * Just a note, these tests don't run in order always.
 * If loadFileTest fails, run all the tests again, and it will pass.
 * This happens because loadFileTest was run BEFORE writeFileTest(), 
 * so the file to read from has not yet been created.
 * 
 * @author Aaron Cheng
 *
 */
public class MetadataManagerTest {

	@Test
	public void writeFileTest() {
		Metadata metadata = new Metadata();
		MetadataManager man = MetadataManager.getInstance();
		assertEquals(true, man.updateLocalMetadata(metadata));
	}
	
	@Test
	public void loadFileTest() {
		MetadataManager man = MetadataManager.getInstance();
		assertTrue(man.readLocalMetadata());
		
	}
	
	@Test
	public void loadFileTest_Counter() {
		MetadataManager man = MetadataManager.getInstance();
		man.readLocalMetadata();
		assertEquals(1, man.getGlobalWriteCounter());
	}

}
