package test;

import static org.junit.Assert.*;

import org.junit.Test;

import cloudsync.client.FileSysPerformer;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;

public class FileSysPerformerTest {
	@Test
	public void testGetAbsoluteFilename() {
		FileSysPerformer fileSysPerformer = null;
		fileSysPerformer = FileSysPerformer.getInstance();
		String absName = fileSysPerformer.getAbsoluteFilename("Desktop\\test.docx");
		assertEquals("C:\\Users\\Tianlai Dong\\Desktop\\test.docx",absName);
	}
	@Test
	public void testGetBaseFilename(){
		FileSysPerformer fileSysPerformer = null;
		fileSysPerformer = FileSysPerformer.getInstance();
		String bsName = fileSysPerformer.getBaseFilename("C:\\Users\\Tianlai Dong\\Desktop\\test.docx");
		assertEquals("Desktop\\test.docx",bsName);
	}
	@Test 
	public void testAddUpdateLocalTask(){
		FileSysPerformer fileSysPerformer = null;
		fileSysPerformer = FileSysPerformer.getInstance();
		Metadata metadata = new Metadata("Desktop\\test.docx");
		metadata.status = STATUS.DELETE;
		fileSysPerformer.addUpdateLocalTask(metadata);
	}
}
