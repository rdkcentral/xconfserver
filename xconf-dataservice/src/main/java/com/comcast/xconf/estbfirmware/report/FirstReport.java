/*******************************************************************************
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.comcast.xconf.estbfirmware.report;

import com.comcast.xconf.estbfirmware.ConfigChangeLog;
import com.comcast.xconf.estbfirmware.ConfigChangeLogService;
import com.comcast.xconf.estbfirmware.LastConfigLog;
import jxl.Workbook;
import jxl.write.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;


@Component
public class FirstReport {

	@Autowired
	private ConfigChangeLogService configChangeLogService;

	private WritableFont headFont = new WritableFont(WritableFont.ARIAL, 12,
			WritableFont.BOLD);
	private WritableCellFormat headFormat = new WritableCellFormat(headFont);

	private WritableFont font = new WritableFont(WritableFont.ARIAL, 12,
			WritableFont.NO_BOLD);
	private WritableCellFormat format = new WritableCellFormat(font);

	public void doReport(Set<String> macAddresses, OutputStream out) {

		WritableWorkbook wb = null;
		try {
			wb = Workbook.createWorkbook(out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		WritableSheet sheet = wb.createSheet("Report", 0);

		addHeadCell(sheet, 0, 0, "estbMac");
		addHeadCell(sheet, 1, 0, "env");
		addHeadCell(sheet, 2, 0, "model");
		addHeadCell(sheet, 3, 0, "firmwareVersion");
		addHeadCell(sheet, 4, 0, "time");
		addHeadCell(sheet, 5, 0, "ipAddress");

		addHeadCell(sheet, 6, 0, "rule type");
		addHeadCell(sheet, 7, 0, "rule name");
		addHeadCell(sheet, 8, 0, "noop");

		addHeadCell(sheet, 9, 0, "filter name");

		addHeadCell(sheet, 10, 0, "firmwareVersion");
		addHeadCell(sheet, 11, 0, "firmwareFilename");
		addHeadCell(sheet, 12, 0, "firmwareLocation");
		addHeadCell(sheet, 13, 0, "firmwareDownloadProtocol");

		addHeadCell(sheet, 14, 0, "lst chg env");
		addHeadCell(sheet, 15, 0, "lst chg model");
		addHeadCell(sheet, 16, 0, "lst chg firmwareVersion");
		addHeadCell(sheet, 17, 0, "lst chg time");
		addHeadCell(sheet, 18, 0, "lst chg ipAddress");

		addHeadCell(sheet, 19, 0, "lst chg rule type");
		addHeadCell(sheet, 20, 0, "lst chg rule name");
		addHeadCell(sheet, 21, 0, "lst chg noop");

		addHeadCell(sheet, 22, 0, "lst chg firmwareVersion");
		addHeadCell(sheet, 23, 0, "lst chg firmwareFilename");
		addHeadCell(sheet, 24, 0, "lst chg firmwareLocation");
		addHeadCell(sheet, 25, 0, "lst chg firmwareDownloadProtocol");

		int i = 1;
		for (String ma : macAddresses) {

			LastConfigLog ll = configChangeLogService.getLastConfigLog(ma);

			if (ll != null) {

                if (ll.getInput() != null) {
                    addCell(sheet, 0, i, ll.getInput().getEstbMac().toString());
                    addCell(sheet, 1, i, ll.getInput().getEnv());
                    addCell(sheet, 2, i, ll.getInput().getModel());
                    addCell(sheet, 3, i, ll.getInput().getFirmwareVersion());
                    addCell(sheet, 4, i, ll.getInput().getTime().toString());
                    addCell(sheet, 5, i, ll.getInput().getIpAddress().toString());
                }

				if (ll.getRule() != null) {
					addCell(sheet, 6, i, ll.getRule().getType());
					addCell(sheet, 7, i, ll.getRule().getName());
					addCell(sheet, 8, i, ll.getRule().isNoop() + "");
				} else {
					addCell(sheet, 6, i, "");
					addCell(sheet, 7, i, "");
					addCell(sheet, 8, i, "");
				}

				if (!ll.getFilters().isEmpty()) {

                    /*
					 * XXX TODO allow for multiple filters. Well, really still
					 * we can have only one matched filter, so no hurry.
					 */


					addCell(sheet, 9, i, ll.getFilters().get(0).getName());
				} else {
					addCell(sheet, 9, i, "");
				}

				if (ll.getConfig() != null) {
					addCell(sheet, 10, i, ll.getConfig().getFirmwareVersion());
					addCell(sheet, 11, i, ll.getConfig().getFirmwareFilename());
					addCell(sheet, 12, i, ll.getConfig().getFirmwareLocation()
							+ "");
					addCell(sheet, 13, i, ll.getConfig()
							.getFirmwareDownloadProtocol() + "");
				} else {
					addCell(sheet, 10, i, "");
					addCell(sheet, 11, i, "");
					addCell(sheet, 12, i, "");
					addCell(sheet, 13, i, "");
				}

				List<ConfigChangeLog> chs = configChangeLogService.getChangeLogsOnly(ma);
				for (ConfigChangeLog cl : chs) {

                    if (ll.getInput() != null) {
                        addCell(sheet, 14, i, ll.getInput().getEnv());
                        addCell(sheet, 15, i, ll.getInput().getModel());
                        addCell(sheet, 16, i, ll.getInput().getFirmwareVersion());
                        addCell(sheet, 17, i, ll.getInput().getTime().toString());
                        addCell(sheet, 18, i, ll.getInput().getIpAddress()
                                .toString());
                    }

					if (ll.getRule() != null) {
						addCell(sheet, 19, i, cl.getRule().getType());
						addCell(sheet, 20, i, cl.getRule().getName());
						addCell(sheet, 21, i, cl.getRule().isNoop() + "");
					} else {
						addCell(sheet, 19, i, "");
						addCell(sheet, 20, i, "");
						addCell(sheet, 22, i, "");
					}

					if (ll.getConfig() != null) {
						addCell(sheet, 22, i, cl.getConfig()
								.getFirmwareVersion());
						addCell(sheet, 23, i, cl.getConfig()
								.getFirmwareFilename());
						addCell(sheet, 24, i, cl.getConfig()
								.getFirmwareLocation());
						addCell(sheet, 25, i, cl.getConfig()
								.getFirmwareDownloadProtocol() + "");
					} else {
						addCell(sheet, 22, i, "");
						addCell(sheet, 23, i, "");
						addCell(sheet, 24, i, "");
						addCell(sheet, 25, i, "");
					}
					break;
				}
				i++;
			}
		}

		try {
			wb.write();
			wb.close();
		} catch (IOException | WriteException e) {
			throw new RuntimeException(e);
		}
	}

	private void addCell(WritableSheet s, int c, int r, String val) {
		try {
			s.addCell(new Label(c, r, val, format));
		} catch (WriteException e) {
			throw new RuntimeException(e);
		}
	}

	private void addHeadCell(WritableSheet s, int c, int r, String heading) {
		try {
			s.addCell(new Label(c, r, heading, headFormat));
		} catch (WriteException e) {
			throw new RuntimeException(e);
		}
	}
}
