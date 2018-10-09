package com.tvd12.ezyfoxserver.client.socket;

import android.util.Log;

import com.tvd12.ezyfoxserver.client.constant.EzyDisconnectReason;
import com.tvd12.ezyfoxserver.client.constant.EzySocketConstants;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class EzySocketReader
		extends EzyAbstractSocketEventHandler {

	protected SocketChannel socketChannel;
	protected final ByteBuffer buffer;
	protected final EzySocketDataHandler socketDataHandler;

	public EzySocketReader(EzySocketDataHandler socketDataHandler) {
		this.socketDataHandler = socketDataHandler;
		this.buffer = ByteBuffer.allocateDirect(getMaxBufferSize());
	}

	@Override
	public void handleEvent() {
		try {
			processSocketChannel();
			Thread.sleep(3L);
		}
		catch(Exception e) {
			Log.i("ezyfox-client","I/O error at socket-reader: " + e.getMessage());
		}
	}
	
	private int getMaxBufferSize() {
		return EzySocketConstants.MAX_READ_BUFFER_SIZE;
	}
	
	private void processSocketChannel() throws Exception {
		if(socketChannel == null)
			return;
		if(!socketChannel.isConnected()) {
			return;
		}
		this.buffer.clear();
		long readBytes = socketChannel.read(buffer);
		if(readBytes == -1L) {
			closeConnection();
		}
		else if(readBytes > 0) {
			processReadBytes();
		}
	}
	
	private void processReadBytes() throws Exception {
		buffer.flip();
		byte[] binary = new byte[buffer.limit()];
		buffer.get(binary);
		socketDataHandler.fireBytesReceived(binary);
	}
	
	private void closeConnection() throws Exception {
		socketChannel.close();
		socketDataHandler.fireSocketDisconnected(EzyDisconnectReason.UNKNOWN);
	}

	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	@Override
	public void destroy() {
		buffer.clear();
	}

	@Override
	public void reset() {
		buffer.clear();
	}
}