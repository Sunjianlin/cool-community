// 文本处理工具函数

/**
 * 去除HTML标签
 * @param {string} html - 包含HTML标签的字符串
 * @returns {string} 去除HTML标签后的纯文本
 */
export const stripHtml = (html) => {
  if (!html) return ''
  return html.replace(/<[^>]*>/g, '')
}

/**
 * 截断文本并去除HTML标签
 * @param {string} text - 文本内容（可能包含HTML标签）
 * @param {number} length - 截断长度
 * @returns {string} 处理后的文本
 */
export const truncateWithStripHtml = (text, length) => {
  if (!text) return ''
  const plainText = stripHtml(text)
  return plainText.length > length ? plainText.substring(0, length) + '...' : plainText
}
