package com.common.share;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;

import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;

@SuppressWarnings({ "serial" })
public class ImageUpload extends VerticalLayout
{
	public Label status = new Label("");

	private ProgressBar pi = new ProgressBar();

	private MyReceiver receiver = new MyReceiver();

	private HorizontalLayout progressLayout = new HorizontalLayout();
	public HorizontalLayout image = new HorizontalLayout();

	public Upload upload = new Upload(null, receiver);

	public String fileName = "", coreName = "";
	public boolean success = false;

	public ImageUpload(String fname)
	{
		coreName = fname;
		fileName = fname;
		setSpacing(true);

		image.setWidth("120px");
		image.setHeight("140px");
		// Slow down the upload
		//receiver.setSlow(true);
		addComponent(image);
		addComponent(status);
		addComponent(upload);
		addComponent(progressLayout);

		// Make uploading start immediately when file is selected
		upload.setImmediate(true);
		upload.setButtonCaption("Select file");

		progressLayout.setSpacing(true);
		progressLayout.setVisible(false);
		progressLayout.addComponent(pi);
		//progressLayout.setComponentAlignment(pi, "middle");

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

		/**
		 * =========== Add needed listener for the upload component: start,
		 * progress, finish, success, fail ===========
		 */

		upload.addStartedListener(new Upload.StartedListener()
		{
			public void uploadStarted(StartedEvent event)
			{
				// This method gets called immediately after upload is started
				upload.setVisible(false);
				progressLayout.setVisible(true);
				pi.setValue(0f);
				//status.setValue("Uploading file \"" + event.getFilename() + "\"");
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
				//status.setValue("Uploading file \"" + event.getFilename() + "\" succeeded");

				String filePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"".replace("\\","/")+"/VAADIN/rpttmp/"+ fileName;
				setImage(filePath);

				success = true;
				//getWindow().executeJavaScript("location.reload();");
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
				// upload.setCaption("Select another file");
			}
		});
	}

	public void setImage(String filePath)
	{
		try
		{
			File file = new File(filePath);
			//System.out.println(filePath);

			Embedded e = new Embedded("Image Uploaded", new FileResource(file));
			e.setWidth("120px");
			e.setHeight("140px");

			image.removeAllComponents();
			image.addComponent(e);
		}
		catch (Exception ex)
		{ System.out.println(ex); }
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
				System.out.println("Error: "+exp);
			}
			fileName = coreName+fname+".jpg";
			FileOutputStream fos = null; // Output stream to write to

			String filePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()+"".replace("\\","/")+"/VAADIN/rpttmp/"+ fileName;
			//System.out.println(filePath);
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