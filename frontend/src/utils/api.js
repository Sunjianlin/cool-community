// API 工具类，统一处理 API 请求并添加 token 认证

/**
 * 发送 API 请求
 * @param {string} url - 请求 URL
 * @param {object} options - 请求选项
 * @returns {Promise} - 请求结果
 */
export async function apiRequest(url, options = {}) {
  // 从 localStorage 获取 token
  const token = localStorage.getItem('token');
  
  // 设置默认请求头
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };
  
  // 如果有 token，添加到请求头
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  // 发送请求
  const response = await fetch(url, {
    ...options,
    headers
  });
  
  // 处理响应
  if (!response.ok) {
    throw new Error(`API 请求失败: ${response.status}`);
  }
  
  return response.json();
}

/**
 * 发送 GET 请求
 * @param {string} url - 请求 URL
 * @param {object} options - 请求选项
 * @returns {Promise} - 请求结果
 */
export async function get(url, options = {}) {
  return apiRequest(url, {
    ...options,
    method: 'GET'
  });
}

/**
 * 发送 POST 请求
 * @param {string} url - 请求 URL
 * @param {object} data - 请求数据
 * @param {object} options - 请求选项
 * @returns {Promise} - 请求结果
 */
export async function post(url, data, options = {}) {
  return apiRequest(url, {
    ...options,
    method: 'POST',
    body: JSON.stringify(data)
  });
}

/**
 * 发送 PUT 请求
 * @param {string} url - 请求 URL
 * @param {object} data - 请求数据
 * @param {object} options - 请求选项
 * @returns {Promise} - 请求结果
 */
export async function put(url, data, options = {}) {
  return apiRequest(url, {
    ...options,
    method: 'PUT',
    body: JSON.stringify(data)
  });
}

/**
 * 发送 DELETE 请求
 * @param {string} url - 请求 URL
 * @param {object} options - 请求选项
 * @returns {Promise} - 请求结果
 */
export async function del(url, options = {}) {
  return apiRequest(url, {
    ...options,
    method: 'DELETE'
  });
}
