package com.spaceflight.pad.filebrowser;

import java.util.Comparator;

import com.spaceflight.pad.object.FileInfo;
public class DefaultSortOrder implements Comparator<FileInfo> {

	public int compare(FileInfo file1, FileInfo file2) {
		// �ļ�������ǰ��
		if (file1.IsDirectory && !file2.IsDirectory) {
			return -1000;
		} else if (!file1.IsDirectory && file2.IsDirectory) {
			return 1000;
		}
		// ��ͬ���Ͱ���������
		return file1.Name.compareTo(file2.Name);
	}
}
