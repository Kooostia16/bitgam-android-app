package com.bitgam.app.xmpp.jingle;

import com.bitgam.app.entities.DownloadableFile;

public interface OnFileTransmissionStatusChanged {
	void onFileTransmitted(DownloadableFile file);

	void onFileTransferAborted();
}
