package test;

import static org.junit.Assert.*;

import org.junit.Test;

import cloudsync.client.ClientSettings;
import cloudsync.client.FileSysPerformer;
import cloudsync.sharedInterface.Metadata;
import cloudsync.sharedInterface.Metadata.STATUS;

public class FileSysPerformerTest {
	/*
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
	*/
	
	@Test 
	public void testAddUpdateLocalTask(){
		ClientSettings cSettings = ClientSettings.getInstance();
		cSettings.setRootDir("C:\\Users\\Tianlai Dong");
		FileSysPerformer fileSysPerformer = FileSysPerformer.getInstance();
		Metadata metadata = new Metadata("Desktop\\1234.txt");
		Metadata metadata2 = new Metadata("Desktop\\123.txt");
		System.out.println(metadata.basename);
		fileSysPerformer.addUpdateLocalTask(metadata);
		fileSysPerformer.addUpdateLocalTask(metadata2);
		System.out.println(metadata.status);
		metadata.status = STATUS.DELETE;
		fileSysPerformer.addUpdateLocalTask(metadata);
		System.out.println(metadata.status);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
