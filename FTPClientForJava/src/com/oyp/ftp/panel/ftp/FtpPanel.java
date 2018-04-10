
package com.oyp.ftp.panel.ftp;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;


import sun.net.TelnetInputStream;
import com.oyp.ftp.FTPClientFrame;
import com.oyp.ftp.panel.FTPTableCellRanderer;
import com.oyp.ftp.utils.FtpClient;
import com.oyp.ftp.utils.FtpFile;

public class FtpPanel extends javax.swing.JPanel {

	FtpClient ftpClient;
	private javax.swing.JButton createFolderButton;
	private javax.swing.JButton delButton;
	private javax.swing.JButton downButton;
	javax.swing.JTable ftpDiskTable;
	private javax.swing.JLabel ftpSelFilePathLabel;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JToolBar toolBar;
	private javax.swing.JButton refreshButton;
	private javax.swing.JButton renameButton;
	FTPClientFrame frame = null;
	Queue<Object[]> queue = new LinkedList<Object[]>();
	private DownThread thread;

	public FtpPanel() {
		initComponents();
	}

	public FtpPanel(FTPClientFrame client_Frame) {
		frame = client_Frame;
		initComponents();
	}

	private void initComponents() {
		ActionMap actionMap = getActionMap();
		actionMap.put("createFolderAction", new CreateFolderAction(this,
				"�����ļ���", null));
		actionMap.put("delAction", new DelFileAction(this, "ɾ��", null));
		actionMap.put("refreshAction", new RefreshAction(this, "ˢ��", null));
		actionMap.put("renameAction", new RenameAction(this, "������", null));
		actionMap.put("downAction", new DownAction(this, "����", null));

		java.awt.GridBagConstraints gridBagConstraints;

		toolBar = new javax.swing.JToolBar();
		delButton = new javax.swing.JButton();
		renameButton = new javax.swing.JButton();
		createFolderButton = new javax.swing.JButton();
		downButton = new javax.swing.JButton();
		refreshButton = new javax.swing.JButton();
		scrollPane = new JScrollPane();
		ftpDiskTable = new JTable();
		ftpDiskTable.setDragEnabled(true);
		ftpSelFilePathLabel = new javax.swing.JLabel();

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Զ��",
				javax.swing.border.TitledBorder.CENTER,
				javax.swing.border.TitledBorder.ABOVE_TOP));
		setLayout(new java.awt.GridBagLayout());

		toolBar.setRollover(true);
		toolBar.setFloatable(false);

		delButton.setText("ɾ��");
		delButton.setFocusable(false);
		delButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		delButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		delButton.setAction(actionMap.get("delAction"));
		toolBar.add(delButton);

		renameButton.setText("������");
		renameButton.setFocusable(false);
		renameButton.setAction(actionMap.get("renameAction"));
		toolBar.add(renameButton);

		createFolderButton.setText("���ļ���");
		createFolderButton.setFocusable(false);
		createFolderButton.setAction(actionMap.get("createFolderAction"));
		toolBar.add(createFolderButton);

		downButton.setText("����");
		downButton.setFocusable(false);
		downButton.setAction(actionMap.get("downAction"));
		toolBar.add(downButton);

		refreshButton.setText("ˢ��");
		refreshButton.setFocusable(false);
		refreshButton.setAction(actionMap.get("refreshAction"));
		toolBar.add(refreshButton);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		add(toolBar, gridBagConstraints);

		ftpDiskTable.setModel(new FtpTableModel());
		ftpDiskTable.setShowHorizontalLines(false);
		ftpDiskTable.setShowVerticalLines(false);
		ftpDiskTable.getTableHeader().setReorderingAllowed(false);
		ftpDiskTable.setDoubleBuffered(true);
		ftpDiskTable.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				ftpDiskTableMouseClicked(evt);
			}
		});
		scrollPane.setViewportView(ftpDiskTable);
		scrollPane.getViewport().setBackground(Color.WHITE);
		//������Ⱦ������Դ��FTP��Դ����������Ⱦ��
		ftpDiskTable.getColumnModel().getColumn(0).setCellRenderer(
				FTPTableCellRanderer.getCellRanderer());
		//RowSorter ��һ��ʵ�֣���ʹ�� TableModel �ṩ����͹��˲�����
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				ftpDiskTable.getModel());
		TableStringConverter converter = new TableConverter();
		//���ø���ֵ��ģ��ת��Ϊ�ַ����Ķ���
		sorter.setStringConverter(converter);
		//���� RowSorter��RowSorter �����ṩ�� JTable ������͹��ˡ� 
		ftpDiskTable.setRowSorter(sorter);
		/**
		 * �ߵ�ָ���е�����˳�򡣵��ô˷���ʱ���������ṩ������Ϊ��
		 * ͨ�������ָ�����Ѿ�����Ҫ�����У���˷����������Ϊ���򣨻򽫽����Ϊ���򣩣�
		 * ����ʹָ���г�Ϊ��Ҫ�����У���ʹ����������˳�����ָ���в���������˷���û���κ�Ч���� 
		 */
		sorter.toggleSortOrder(0);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(scrollPane, gridBagConstraints);

		ftpSelFilePathLabel.setBorder(javax.swing.BorderFactory
				.createEtchedBorder());
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		add(ftpSelFilePathLabel, gridBagConstraints);
	}

	/**
	 * ��񵥻���˫���¼��Ĵ�������
	 */
	private void ftpDiskTableMouseClicked(java.awt.event.MouseEvent evt) {
		int selectedRow = ftpDiskTable.getSelectedRow();
		Object value = ftpDiskTable.getValueAt(selectedRow, 0);
		if (value instanceof FtpFile) {
			FtpFile selFile = (FtpFile) value;
			ftpSelFilePathLabel.setText(selFile.getAbsolutePath());
			if (evt.getClickCount() >= 2) { //˫�����
				if (selFile.isDirectory()) {
					try {
						ftpClient.cd(selFile.getAbsolutePath());
						listFtpFiles(ftpClient.list());
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * ��ȡFTP�ļ������ķ���
	 * @param list
	 *            ��ȡFTP��������Դ�б��������
	 */
	public synchronized void listFtpFiles(final TelnetInputStream list) {
		// ��ȡ��������ģ��
		final DefaultTableModel model = (DefaultTableModel) ftpDiskTable
				.getModel();
		model.setRowCount(0);
		// ����һ���߳���
		Runnable runnable = new Runnable() {
			public synchronized void run() {
				ftpDiskTable.clearSelection();
				try {
					String pwd = getPwd(); // ��ȡFTP�������ĵ�ǰ�ļ���
					model.addRow(new Object[] { new FtpFile(".", pwd, true),
							"", "" }); // ��ӡ�.������
					model.addRow(new Object[] { new FtpFile("..", pwd, true),
							"", "" }); // ��ӡ�..������
					/*
					byte[]names=new byte[2048];
					int bufsize=0;
					bufsize=list.read(names, 0, names.length);
//					list.close();
					int i=0,j=0;
					while(i<bufsize){
						char bc=(char)names[i];
						System.out.print(i+" "+bc+" ");
						//�ļ����������п�ʼ������Ϊj,i-jΪ�ļ����ĳ��ȣ��ļ����������еĽ����±�Ϊi-1
						if (names[i]==13) {
//							System.out.println("j:"+j+" i:"+i+ " i-j:"+(i-j));
							String temName=new String(names,j,i-j);
							System.out.println("temName="+temName);
							j=i+2;
						}
						i=i+1;
					}
					*/
					/* 	���и�ʽӦ�������¸�ʽ���ַ���	���Ϊ:
						0 -: 1 r: 2 w: 3 x: 4 -: 5 -: 6 -: 7 -: 8 -: 9 -: 10  : 11 1: 12  : 13 u: 14 s: 15 e: 16 r: 17  : 18 g: 19 r: 20 o: 21 u: 22 p: 23  : 24  : 25  : 26  : 27  : 28  : 29  : 30  : 31  : 32 6: 33 7: 34 8: 35 4: 36 3: 37 0: 38  : 39 A: 40 p: 41 r: 42  : 43 1: 44 6: 45  : 46 2: 47 1: 48 :: 49 4: 50 6: 51  : 52 F: 53 T: 54 P: 55 ?: 56 ?: 57 ?: 58 ?: 59 ?: 60 ?: 61 ?: 62 ?: 63 ?: 64 ?: 65 ?: 66 ?: 67 ?: 68 ?: 69 ?: 70 ?: 71 ?: 72 ?: 73 .: 74 p: 75 d: 76 f: 77 
						 
						  -rwx------ 1 user group         678430 Apr 16 21:46 FTP�ͻ��˵������ʵ��.pdf
						  -rwx------ 1 user group       87504927 Apr 18 22:50 VC.�������(����)[www.xuexi111.com].pdf
						  -rwx------ 1 user group          57344 Apr 18 05:32 ��Ѷ����2013ʵϰ����ƸTST�Ƽ�ģ��.xls
						
						 *<br>d			��ʾĿ¼	
						 * <br>-			��ʾ�ļ�
						 * <br>rw-rw-rw-	��ʾȨ������
						
						dateStr:39-51
						sizeOrDir��23-38
						fileName:52-^
					*/
					
					/*********************************************************/
					byte[]names=new byte[2048];
					int bufsize=0;
					bufsize=list.read(names, 0, names.length);
					int i=0,j=0;
					while(i<bufsize){
						//�ַ�ģʽΪ10��������ģʽΪ13
//						if (names[i]==10) {
						if (names[i]==13) {
							//��ȡ�ַ��� -rwx------ 1 user group          57344 Apr 18 05:32 ��Ѷ����2013ʵϰ����ƸTST�Ƽ�ģ��.xls
							//�ļ����������п�ʼ������Ϊj,i-jΪ�ļ����ĳ��ȣ��ļ����������еĽ����±�Ϊi-1
							String fileMessage = new String(names,j,i-j);
							if(fileMessage.length() == 0){
								System.out.println("fileMessage.length() == 0");
								break;
							}
							//���տո�fileMessage��Ϊ������ȡ�����Ϣ
							// ������ʽ  \s��ʾ�ո񣬣�1������ʾ1һ������ 
							if(!fileMessage.split("\\s+")[8].equals(".") && !fileMessage.split("\\s+")[8].equals("..")){
								/**�ļ���С*/
								String sizeOrDir="";
								if (fileMessage.startsWith("d")) {//�����Ŀ¼
									sizeOrDir="<DIR>";
								}else if (fileMessage.startsWith("-")) {//������ļ�
									sizeOrDir=fileMessage.split("\\s+")[4];
								}
								/**�ļ���*/
								String fileName=fileMessage.split("\\s+")[8];
								/**�ļ�����*/
								String dateStr =fileMessage.split("\\s+")[5] +" "+fileMessage.split("\\s+")[6]+" " +fileMessage.split("\\s+")[7];
//								System.out.println("sizeOrDir="+sizeOrDir);
//								System.out.println("fileName="+fileName); 
//								System.out.println("dateStr="+dateStr);
								
								FtpFile ftpFile = new FtpFile();
								// ��FTPĿ¼��Ϣ��ʼ����FTP�ļ�������
								ftpFile.setLastDate(dateStr);
								ftpFile.setSize(sizeOrDir);
								ftpFile.setName(fileName);
								ftpFile.setPath(pwd);
								// ���ļ���Ϣ��ӵ������
								model.addRow(new Object[] { ftpFile, ftpFile.getSize(),
										dateStr });
							}
							
//							j=i+1;//��һ��λ��Ϊ�ַ�ģʽ
							j=i+2;//��һ��λ��Ϊ������ģʽ
						}
						i=i+1;
					}
					list.close();
					
					/**********************************************************************
					//����ķ���̫����,�������
					BufferedReader br = new BufferedReader(
							new InputStreamReader(list)); // �����ַ�������
					String data = null;
					// ��ȡ�������е��ļ�Ŀ¼
					while ((data = br.readLine()) != null) {
						// ����FTP�ļ�����
						FtpFile ftpFile = new FtpFile();
						// ��ȡFTP������Ŀ¼��Ϣ
						    String dateStr = data.substring(39, 51).trim();
							String sizeOrDir = data.substring(23, 38).trim();
							String fileName = data.substring(52, data.length())
									.trim();
							// ��FTPĿ¼��Ϣ��ʼ����FTP�ļ�������
							ftpFile.setLastDate(dateStr);
							ftpFile.setSize(sizeOrDir);
							ftpFile.setName(fileName);
							ftpFile.setPath(pwd);
							// ���ļ���Ϣ��ӵ������
							model.addRow(new Object[] { ftpFile, ftpFile.getSize(),
									dateStr });
						}
						br.close(); // �ر�������
					**********************************************************************/
					
				} catch (IOException ex) {
					Logger.getLogger(FTPClientFrame.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}
		};
		if (SwingUtilities.isEventDispatchThread()) // �����̶߳���
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}

	
	/**
	 * ����FTP���ӣ����������ض����̵߳ķ���
	 */
	public void setFtpClient(FtpClient ftpClient) {
		this.ftpClient = ftpClient;
		// ��30��Ϊ��������������ͨѶ
		final Timer timer = new Timer(3000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					final FtpClient ftpClient = FtpPanel.this.ftpClient;
					if (ftpClient != null && ftpClient.serverIsOpen()) {
						ftpClient.noop();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		timer.start();
		startDownThread();
	}

	/**
	 * ˢ��FTP��Դ�������ĵ�ǰ�ļ���
	 */
	public void refreshCurrentFolder() {
		try {
			 // ��ȡ�������ļ��б�
			TelnetInputStream list = ftpClient.list();
			listFtpFiles(list); // ���ý�������
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ʼ���ض����߳�
	 */
	private void startDownThread() {
		if (thread != null)
			thread.stopThread();
		thread = new DownThread(this);
		thread.start();
	}

	/**
	 * ֹͣ���ض����߳�
	 */
	public void stopDownThread() {
		if (thread != null) {
			thread.stopThread();
			ftpClient = null;
		}
	}

	public String getPwd() {
		String pwd = null;
		try {
			pwd = ftpClient.pwd();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pwd;
	}

	public Queue<Object[]> getQueue() {
		return queue;
	}

	/**
	 * ���FTP��Դ������ݵķ���
	 */
	public void clearTable() {
		FtpTableModel model = (FtpTableModel) ftpDiskTable.getModel();
		model.setRowCount(0);
	}
}
