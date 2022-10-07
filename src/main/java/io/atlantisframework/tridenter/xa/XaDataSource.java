/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.atlantisframework.tridenter.xa;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.AbstractDataSource;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.jdbc.JdbcUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaDataSource
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
@Slf4j
public class XaDataSource extends AbstractDataSource implements XaStateChangeListener {

	private final DataSource targetDataSource;

	public XaDataSource(DataSource targetDataSource) {
		this.targetDataSource = targetDataSource;
	}

	private final Map<String, XaConnectionHolder> cache = new ConcurrentHashMap<String, XaConnectionHolder>();

	@Override
	public void prepare(XaMessage event) throws Exception {
		XaConnectionHolder holder = cache.get(event.getXaId());
		if (holder != null) {
			if (holder.isClosed()) {
				throw new SQLException("Connection is closed");
			}
			if (log.isTraceEnabled()) {
				log.trace("[XA:{}:{}] is prepared", event.getXaId(), event.getSerial());
			}
		}
	}

	@Override
	public synchronized void commit(XaMessage event) throws Exception {
		XaConnectionHolder holder = cache.get(event.getXaId());
		if (holder != null) {
			holder.commit();
			if (log.isTraceEnabled()) {
				log.trace("[XA:{}:{}] commit", event.getXaId(), event.getSerial());
			}
		}
	}

	@Override
	public synchronized void rollback(XaMessage event) throws Exception {
		XaConnectionHolder holder = cache.get(event.getXaId());
		if (holder != null) {
			holder.rollback();
			if (log.isTraceEnabled()) {
				log.trace("[XA:{}:{}] rollback", event.getXaId(), event.getSerial());
			}
		}
	}

	@Override
	public synchronized void rollback(XaMessage event, Exception e) throws Exception {
		if (event.getState() != XaState.ROLLBACK) {
			XaConnectionHolder holder = cache.get(event.getXaId());
			if (holder != null) {
				holder.rollback();
				if (log.isTraceEnabled()) {
					log.trace("[XA:{}:{}] rollback", event.getXaId(), event.getSerial());
				}
			}
		}
	}

	@Override
	public void finish(XaMessage event) throws Exception {
		XaConnectionHolder connectionHolder = cache.remove(event.getXaId());
		if (connectionHolder != null) {
			connectionHolder.close();
			if (log.isTraceEnabled()) {
				log.trace("[XA:{}:{}] finish", event.getXaId(), event.getSerial());
			}
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = targetDataSource.getConnection();
		if (XaId.has()) {
			return MapUtils.get(cache, XaId.get(), () -> {
				return new XaConnectionHolder(connection);
			}).getDelegateConnection();
		}
		return connection;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		Connection connection = targetDataSource.getConnection(username, password);
		if (XaId.has()) {
			return MapUtils.get(cache, XaId.get(), () -> {
				return new XaConnectionHolder(connection);
			}).getDelegateConnection();
		}
		return connection;
	}

	/**
	 * 
	 * XaConnectionHolder
	 *
	 * @author Fred Feng
	 *
	 * @since 2.0.4
	 */
	class XaConnectionHolder implements InvocationHandler {

		private final Connection targetConnection;
		private final Connection delegateConnection;

		XaConnectionHolder(Connection connection) {
			this.targetConnection = connection;
			JdbcUtils.disableCommitQuietly(targetConnection);
			this.delegateConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
					new Class<?>[] { Connection.class }, this);
		}

		public Connection getTargetConnection() {
			return targetConnection;
		}

		public Connection getDelegateConnection() {
			return delegateConnection;
		}

		public boolean isClosed() throws SQLException {
			return targetConnection.isClosed();
		}

		public void commit() throws SQLException {
			JdbcUtils.commit(targetConnection);
		}

		public void rollback() throws SQLException {
			JdbcUtils.rollback(targetConnection);
		}

		public void close() throws SQLException {
			JdbcUtils.close(targetConnection);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			if ("commit".equals(methodName) || "rollback".equals(methodName) || "close".equals(methodName)) {
				if (log.isDebugEnabled()) {
					log.debug("Skip invocation: {}", methodName);
				}
				return null;
			}
			return method.invoke(targetConnection, args);
		}

	}

}
