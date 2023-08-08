package com.common.share;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;

import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings({ "serial", "deprecation" })
public class FileUpload extends VerticalLayout
{
	public Label status = new Label("<font size=1px>(Select .pdf/.jpg file)</font>", ContentMode.HTML);
	private ProgressIndicator pi = new ProgressIndicator();
	private MyReceiver receiver = new MyReceiver();
	private HorizontalLayout progressLayout = new HorizontalLayout();

	public Upload upload = new Upload(null, receiver);

	public String fileName = "";
	private String coreName = "";

	public boolean actionCheck = false;

	SessionBean sessionBean;
	public FileUpload(String fname, String caption) 
	{
		coreName = fname;
		fileName = fname;
		setSpacing(true);

		// Slow down the upload
		//receiver.setSlow(true);

		//addComponent(status);
		addComponent(upload);
		addComponent(progressLayout);

		// Make uploading start immediately when file is selected
		upload.setImmediate(true);
		upload.setStyleName(ValoTheme.BUTTON_SMALL);
		upload.setWidth("100%");
		//upload.setIcon(FontAwesome.FILE_IMAGE_O);
		upload.setDescription("Click to select file (.jpg or .pdf)");
		upload.setButtonCaption(caption);

		progressLayout.setSpacing(true);
		progressLayout.setVisible(false);
		progressLayout.addComponent(pi);
		progressLayout.setComponentAlignment(pi, Alignment.MIDDLE_LEFT);

		final Button cancelProcessing = new Button("Cancel");
		cancelProcessing.addClickListener(new Button.ClickListener()
		{
			public void buttonClick(ClickEvent event)
			{
				upload.interruptUpload();
			}
		});
		cancelProcessing.setStyleName("small");
		progressLayout.addComponent(cancelProcessing);

		upload.addStartedListener(new Upload.StartedListener()
		{
			public void uploadStarted(StartedEvent event)
			{
				// This method gets called immediatedly after upload is started
				upload.setVisible(false);
				progressLayout.setVisible(true);
				pi.setValue(0f);
				pi.setPollingInterval(500);
				status.setValue("Uploading file \"" + event.getFilename() + "\"");

				fileName = event.getFilename();
			}
		});

		upload.addProgressListener(new Upload.ProgressListener()
		{
			public void updateProgress(long readBytes, long contentLength)
			{
				// This method gets called several times during the update
				pi.setValue(new Float(readBytes / (float) contentLength));
			}
		});

		upload.addSucceededListener(new Upload.SucceededListener()
		{
			public void uploadSucceeded(SucceededEvent event)
			{
				// This method gets called when the upload finished successfully
				status.setValue("Upload \"" + event.getFilename() + "\" succeeded");
				actionCheck = true;
			}
		});

		upload.addFailedListener(new Upload.FailedListener()
		{
			public void uploadFailed(FailedEvent event)
			{
				// This method gets called when the upload failed
				status.setValue("Uploading interrupted");
			}
		});

		upload.addFinishedListener(new Upload.FinishedListener()
		{
			public void uploadFinished(FinishedEvent event)
			{
				// This method gets called always when the upload finished,
				// either succeeding or failing
				progressLayout.setVisible(false);
				upload.setVisible(true);
			}
		});
	}

	public class MyReceiver implements Receiver
	{
		Random r = new Random();
		public OutputStream receiveUpload(String f, String mimetype)
		{
			String fname = r.nextInt(10000)+"";
			try
			{
				String exist = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"".replace("\\","/")+"/VAADIN/rpttmp/"+ fileName;
				File f1 = new File(exist);
				if(f1.isFile())
				{
					f1.delete();
				}
			}
			catch(Exception exp)
			{
				System.out.println(exp);
			}

			if (fileName.endsWith(".jpg"))
			{
				fileName = coreName+fname+".jpg";
			}
			else if (fileName.endsWith(".pdf"))
			{
				fileName = coreName+fname+".pdf";
			}
			else if (fileName.endsWith(".xls"))
			{
				fileName = coreName+fname+".xls";
			}

			FileOutputStream fos = null; // Output stream to write to

			String filePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"".replace("\\","/")+"/VAADIN/rpttmp/"+ fileName;
			File file = new File(filePath);
			try
			{
				// Open the file for writing.
				fos = new FileOutputStream(file);
			}
			catch (final java.io.FileNotFoundException e)
			{
				// Error while opening the file. Not reported here.
				e.printStackTrace();
				return null;
			}
			return fos; // Return the output stream to write to
		}
	}
}