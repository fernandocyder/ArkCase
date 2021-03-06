package com.armedia.arkcase.uitests.complaints;

import com.armedia.arkcase.uitests.base.ArkCaseAuthentication;
import com.armedia.arkcase.uitests.base.ArkCaseTestBase;
import com.armedia.arkcase.uitests.base.ArkCaseTestUtils;
import com.armedia.arkcase.uitests.group.SmokeTests;
import com.armedia.arkcase.uitests.task.TaskPage;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;

public class ComplaintsTests extends ArkCaseTestBase
{

    ComplaintPage complaint = PageFactory.initElements(driver, ComplaintPage.class);
    ComplaintsPage complaints = PageFactory.initElements(driver, ComplaintsPage.class);
    TaskPage task = PageFactory.initElements(driver, TaskPage.class);

    @Test
    @Category({ SmokeTests.class })
    public void createNewComplaintAddNote() throws InterruptedException
    {

        Thread.sleep(10000);
        complaint.clickNewButton();
        Thread.sleep(5000);
        complaint.clickNewComplain();
        Thread.sleep(15000);
        driver.switchTo().frame(complaint.firstIfarme);
        Thread.sleep(3000);
        driver.switchTo().frame(complaint.secondIframe);
        Thread.sleep(3000);
        complaint.verifyNewComplaintPage();
        complaint.clickInitiatorFirstName();
        Thread.sleep(2000);
        complaint.setInitiatorFirstName("Milan");
        Thread.sleep(3000);
        complaint.clickInitiatorLastName();
        Thread.sleep(3000);
        complaint.setInitiatorLastName("Jovanovski");
        Thread.sleep(3000);
        complaint.clickIncidentTab();
        complaint.clickIncidentTab();
        Thread.sleep(3000);
        complaint.clickIncidentCategory();
        Thread.sleep(3000);
        complaint.selectAgricultural();
        Thread.sleep(3000);
        complaint.clickComplaintTitle();
        complaint.setComplaintTitle("Milan's Test Add Note");
        Thread.sleep(3000);
        complaint.clickPeopleTab();
        complaint.clickPeopleTab();
        Thread.sleep(4000);
        complaint.clickSelectparticipantType();
        Thread.sleep(3000);
        complaint.selectOwner();
        Thread.sleep(3000);
        complaint.clickSelectParticipant();
        Thread.sleep(3000);
        complaint.verifyAddpersonPopUp();
        complaint.setUserSearch("samuel");
        Thread.sleep(3000);
        complaint.clickGoButton();
        Thread.sleep(5000);
        complaint.verifyError();
        complaint.verifySearchedUser("Samuel Supervisor");
        Thread.sleep(2000);
        complaint.clickSearchedUser();
        Thread.sleep(3000);
        complaint.clickAddButton();
        Thread.sleep(4000);
        complaint.clickSubmitButton();
        Thread.sleep(20000);
        driver.switchTo().defaultContent();
        complaints.clickSortBtn();
        Thread.sleep(3000);
        complaints.sortDateDesc();
        Thread.sleep(4000);
        complaints.clickFirstComplaint();
        Thread.sleep(4000);
        complaints.verifyComplaintTitle("Milan's Test Add Note");
        complaints.clickNoteLink();
        Thread.sleep(4000);
        complaints.verifyNotesTableTitle();
        complaints.clickAddNoteButton();
        Thread.sleep(4000);
        complaints.verifyAddNotePopUp();
        complaints.setNoteTextArea("note");
        Thread.sleep(3000);
        complaints.clickSaveButton();
        Thread.sleep(4000);
        complaints.verifyAddNotePopUpDisapierd();
        complaints.verifyIfNoteIsCreated();
        complaints.verifyCreatedNote("note", "Samuel Supervisor");
        ArkCaseAuthentication.logOut(driver);

    }

    @Test
    @Category({ SmokeTests.class })
    public void createNewComplaintAddDocument() throws InterruptedException, IOException, AWTException
    {

        Thread.sleep(10000);
        complaint.clickNewButton();
        Thread.sleep(5000);
        complaint.clickNewComplain();
        Thread.sleep(20000);
        driver.switchTo().frame(complaint.firstIfarme);
        Thread.sleep(3000);
        driver.switchTo().frame(complaint.secondIframe);
        Thread.sleep(3000);
        complaint.verifyNewComplaintPage();
        complaint.clickInitiatorFirstName();
        Thread.sleep(2000);
        complaint.setInitiatorFirstName("Milan");
        Thread.sleep(3000);
        complaint.clickInitiatorLastName();
        Thread.sleep(3000);
        complaint.setInitiatorLastName("Jovanovski");
        Thread.sleep(3000);
        complaint.clickIncidentTab();
        complaint.clickIncidentTab();
        Thread.sleep(3000);
        complaint.clickIncidentCategory();
        Thread.sleep(3000);
        complaint.selectAgricultural();
        Thread.sleep(3000);
        complaint.clickComplaintTitle();
        complaint.setComplaintTitle("Milan's Test Add Document");
        Thread.sleep(3000);
        complaint.clickPeopleTab();
        complaint.clickPeopleTab();
        Thread.sleep(4000);
        complaint.clickSelectparticipantType();
        Thread.sleep(3000);
        complaint.selectOwner();
        Thread.sleep(3000);
        complaint.clickSelectParticipant();
        Thread.sleep(3000);
        complaint.verifyAddpersonPopUp();
        complaint.setUserSearch("samuel");
        Thread.sleep(3000);
        complaint.clickGoButton();
        Thread.sleep(5000);
        complaint.verifyError();
        complaint.verifySearchedUser("Samuel Supervisor");
        Thread.sleep(2000);
        complaint.clickSearchedUser();
        Thread.sleep(3000);
        complaint.clickAddButton();
        Thread.sleep(4000);
        complaint.clickSubmitButton();
        Thread.sleep(20000);
        driver.switchTo().defaultContent();
        complaints.clickSortBtn();
        Thread.sleep(3000);
        complaints.sortDateDesc();
        Thread.sleep(4000);
        complaints.clickFirstComplaint();
        Thread.sleep(4000);
        complaints.verifyComplaintTitle("Milan's Test Add Document");
        complaints.clickDocumentsLink();
        Thread.sleep(4000);
        complaints.refreshPageBtn.click();
        Thread.sleep(4000);
        driver.navigate().refresh();
        Thread.sleep(10000);
        complaints.performRightClickOnRoot();
        Thread.sleep(3000);
        complaints.checkIfRightClickOnRootIsWorking();
        complaints.clickNewDocument();
        Thread.sleep(4000);
        complaints.verifyNewDocumentMenu();
        complaints.clickNewDocumentOther();
        Thread.sleep(5000);
        ArkCaseTestUtils.uploadDocx();
        Thread.sleep(10000);
        complaints.verifyIfDocumentIsCreated();
        complaints.verifyCreatedDocument("ArkCaseTesting.docx", "Other", "Samuel Supervisor", "1.0", "ACTIVE");
        complaints.clickNoteLink();
        Thread.sleep(5000);
        ArkCaseAuthentication.logOut(driver);

    }

    @Test
    @Category({ SmokeTests.class })
    public void createNewComplaintVerifyDetailsAddImage() throws InterruptedException, IOException, AWTException
    {

        Thread.sleep(10000);
        complaint.clickNewButton();
        Thread.sleep(5000);
        complaint.clickNewComplain();
        Thread.sleep(20000);
        driver.switchTo().frame(complaint.firstIfarme);
        Thread.sleep(3000);
        driver.switchTo().frame(complaint.secondIframe);
        Thread.sleep(3000);
        complaint.verifyNewComplaintPage();
        complaint.clickInitiatorFirstName();
        Thread.sleep(2000);
        complaint.setInitiatorFirstName("Milan");
        Thread.sleep(3000);
        complaint.clickInitiatorLastName();
        Thread.sleep(3000);
        complaint.setInitiatorLastName("Jovanovski");
        Thread.sleep(3000);
        complaint.clickIncidentTab();
        complaint.clickIncidentTab();
        Thread.sleep(3000);
        complaint.clickIncidentCategory();
        Thread.sleep(3000);
        complaint.selectAgricultural();
        Thread.sleep(3000);
        complaint.clickComplaintTitle();
        complaint.setComplaintTitle("Milan's Test Add Image");
        Thread.sleep(3000);
        complaint.clickPeopleTab();
        complaint.clickPeopleTab();
        Thread.sleep(4000);
        complaint.clickSelectparticipantType();
        Thread.sleep(3000);
        complaint.selectOwner();
        Thread.sleep(3000);
        complaint.clickSelectParticipant();
        Thread.sleep(3000);
        complaint.verifyAddpersonPopUp();
        complaint.setUserSearch("samuel");
        Thread.sleep(3000);
        complaint.clickGoButton();
        Thread.sleep(5000);
        complaint.verifyError();
        complaint.verifySearchedUser("Samuel Supervisor");
        Thread.sleep(2000);
        complaint.clickSearchedUser();
        Thread.sleep(3000);
        complaint.clickAddButton();
        Thread.sleep(4000);
        complaint.clickSubmitButton();
        Thread.sleep(20000);
        driver.switchTo().defaultContent();
        complaints.clickSortBtn();
        Thread.sleep(3000);
        complaints.sortDateDesc();
        Thread.sleep(4000);
        complaints.clickFirstComplaint();
        Thread.sleep(4000);
        complaints.verifyComplaintTitle("Milan's Test Add Image");
        Thread.sleep(3000);
        complaints.clickDetailsLink();
        Thread.sleep(3000);
        complaints.verifyDetailsTitle();
        complaints.clickInsertPictureBtn();
        Thread.sleep(3000);
        complaints.verifyInsertImagePopUp();
        Thread.sleep(3000);
        complaints.clickBrowseButton();
        Thread.sleep(3000);
        ArkCaseTestUtils.uploadFile("png");
        // ArkCaseTestUtils.uploadPNGPicture();
        Thread.sleep(5000);
        complaints.verifyUploadedImage();
        complaints.detailsSaveBtn.click();
        Thread.sleep(4000);
        complaints.documentLink.click();
        Thread.sleep(5000);
        ArkCaseAuthentication.logOut(driver);

    }

    @Test
    @Category({ SmokeTests.class })
    public void createNewComplaintVerifyAddTask() throws InterruptedException, IOException, AWTException
    {

        Thread.sleep(10000);
        complaint.clickNewButton();
        Thread.sleep(5000);
        complaint.clickNewComplain();
        Thread.sleep(20000);
        driver.switchTo().frame(complaint.firstIfarme);
        Thread.sleep(3000);
        driver.switchTo().frame(complaint.secondIframe);
        Thread.sleep(3000);
        complaint.verifyNewComplaintPage();
        complaint.clickInitiatorFirstName();
        Thread.sleep(2000);
        complaint.setInitiatorFirstName("Milan");
        Thread.sleep(3000);
        complaint.clickInitiatorLastName();
        Thread.sleep(3000);
        complaint.setInitiatorLastName("Jovanovski");
        Thread.sleep(3000);
        complaint.clickIncidentTab();
        complaint.clickIncidentTab();
        Thread.sleep(3000);
        complaint.clickIncidentCategory();
        Thread.sleep(3000);
        complaint.selectAgricultural();
        Thread.sleep(3000);
        complaint.clickComplaintTitle();
        complaint.setComplaintTitle("Milan's Test Add task");
        Thread.sleep(3000);
        complaint.clickPeopleTab();
        complaint.clickPeopleTab();
        Thread.sleep(4000);
        complaint.clickSelectparticipantType();
        Thread.sleep(3000);
        complaint.selectOwner();
        Thread.sleep(3000);
        complaint.clickSelectParticipant();
        Thread.sleep(3000);
        complaint.verifyAddpersonPopUp();
        complaint.setUserSearch("samuel");
        Thread.sleep(3000);
        complaint.clickGoButton();
        Thread.sleep(5000);
        complaint.verifyError();
        complaint.verifySearchedUser("Samuel Supervisor");
        Thread.sleep(2000);
        complaint.clickSearchedUser();
        Thread.sleep(3000);
        complaint.clickAddButton();
        Thread.sleep(4000);
        complaint.clickSubmitButton();
        Thread.sleep(20000);
        driver.switchTo().defaultContent();
        complaints.clickSortBtn();
        Thread.sleep(3000);
        complaints.sortDateDesc();
        Thread.sleep(4000);
        complaints.clickFirstComplaint();
        Thread.sleep(4000);
        complaints.verifyComplaintTitle("Milan's Test Add task");
        Thread.sleep(4000);
        complaints.clickTaskLink();
        Thread.sleep(6000);
        complaints.verifyTaskTableTitle();
        complaints.clickAddTaskButton();
        Thread.sleep(10000);
        task.assignTo("samuel");
        Thread.sleep(4000);
        task.typeSubject("associate with complaint");
        Thread.sleep(3000);
        task.typeDuedate("07/28/2016");
        Thread.sleep(3000);
        task.saveButtonClick();
        Thread.sleep(7000);
        complaints.verifyComplaintTitleInTasksPage("Milan's Test Add task");
        complaints.clickComplaintTitleInTasksPage();
        Thread.sleep(7000);
        complaints.clickTaskLink();
        complaints.refreshPageBtn.click();
        complaints.verifyIfTaskIsCreated();
        ArkCaseAuthentication.logOut(driver);

    }
}
