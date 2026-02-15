<template>
  <div class="admin-page">
    <h2 class="page-title">管理中心</h2>
    
    <!-- 管理导航 -->
    <div class="admin-nav">
      <button 
        class="admin-nav-btn" 
        v-for="navItem in navItems" 
        :key="navItem.id"
        :class="{ active: currentNav === navItem.id }"
        @click="currentNav = navItem.id"
      >
        {{ navItem.name }}
      </button>
    </div>
    
    <!-- 话题管理 -->
    <div v-if="currentNav === 'topics'" class="admin-section">
      <div class="section-header">
        <h3 class="section-title">话题管理</h3>
        <button class="btn btn-primary" @click="showTopicDialog()">添加话题</button>
      </div>
      <el-table :data="topics" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="postCount" label="帖子数" width="80" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="showTopicDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteTopicItem(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 产品管理 -->
    <div v-if="currentNav === 'products'" class="admin-section">
      <div class="section-header">
        <h3 class="section-title">产品管理</h3>
        <button class="btn btn-primary" @click="showProductDialog()">添加产品</button>
      </div>
      
      <div class="filter-bar" style="margin-bottom: 16px; display: flex; gap: 12px; flex-wrap: wrap;">
        <el-select v-model="productFilters.categoryId" placeholder="选择品类" clearable style="width: 150px" @change="handleProductFilterChange">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
        <el-select v-model="productFilters.brand" placeholder="选择品牌" clearable style="width: 150px" @change="handleProductFilterChange">
          <el-option v-for="brand in brands" :key="brand.id" :label="brand.name" :value="brand.name" />
        </el-select>
        <el-input v-model="productFilters.keyword" placeholder="搜索产品" style="width: 200px" @input="handleProductFilterChange">
          <template #prefix>🔍</template>
        </el-input>
      </div>
      
      <el-table :data="products" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <img :src="scope.row.image || defaultProductImage" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px;" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column label="品类" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.category" size="small">{{ scope.row.category.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="showProductDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteProductItem(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-if="productPage.total > 0"
        style="margin-top: 16px; justify-content: center"
        layout="prev, pager, next, total"
        :total="productPage.total"
        :page-size="productPage.size"
        :current-page="productPage.page"
        @current-change="handleProductPageChange"
      />
    </div>
    
    <!-- 品牌管理 -->
    <div v-if="currentNav === 'brands'" class="admin-section">
      <div class="section-header">
        <h3 class="section-title">品牌管理</h3>
        <button class="btn btn-primary" @click="showBrandDialog()">添加品牌</button>
      </div>
      <el-table :data="brands" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="Logo" width="80">
          <template #default="scope">
            <img v-if="scope.row.logo" :src="scope.row.logo" style="width: 40px; height: 40px; object-fit: contain; border-radius: 4px;" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="showBrandDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteBrandItem(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 用户管理 -->
    <div v-if="currentNav === 'users'" class="admin-section">
      <h3 class="section-title">用户管理</h3>
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <img :src="scope.row.avatar || defaultAvatar" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="bio" label="个人简介" />
        <el-table-column label="角色" width="150">
          <template #default="scope">
            <el-tag v-for="role in scope.row.roles" :key="role.id" size="small" style="margin-right: 4px;">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isBanned" type="danger" size="small">已禁言</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="banUserItem(scope.row.id)" v-if="!scope.row.isBanned">禁言</el-button>
            <el-button size="small" type="success" @click="unbanUserItem(scope.row.id)" v-else>解除</el-button>
            <el-button size="small" type="danger" @click="deleteUserItem(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 帖子管理 -->
    <div v-if="currentNav === 'posts'" class="admin-section">
      <h3 class="section-title">帖子管理</h3>
      <el-table :data="posts" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="content" label="内容">
          <template #default="scope">
            <span style="display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
              {{ scope.row.content }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="user.username" label="作者" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'APPROVED'" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="scope.row.status === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
            <el-tag v-else type="warning" size="small">待审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="success" @click="approvePostItem(scope.row.id)" v-if="scope.row.status !== 'APPROVED'">通过</el-button>
            <el-button size="small" type="danger" @click="rejectPostItem(scope.row.id)" v-if="scope.row.status !== 'REJECTED'">拒绝</el-button>
            <el-button size="small" type="danger" @click="deletePostItem(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 话题对话框 -->
    <el-dialog v-model="topicDialogVisible" :title="editingTopic.id ? '编辑话题' : '添加话题'" width="500px">
      <el-form :model="editingTopic" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editingTopic.name" placeholder="话题名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editingTopic.description" type="textarea" rows="3" placeholder="话题描述" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="editingTopic.category" placeholder="话题分类" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topicDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTopic" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 产品对话框 -->
    <el-dialog v-model="productDialogVisible" :title="editingProduct.id ? '编辑产品' : '添加产品'" width="700px">
      <el-form :model="editingProduct" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editingProduct.name" placeholder="产品名称" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="editingProduct.brand" placeholder="选择品牌" style="width: 100%" allow-create filterable>
            <el-option v-for="brand in brands" :key="brand.id" :label="brand.name" :value="brand.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="品类">
          <el-select v-model="editingProduct.categoryId" placeholder="选择品类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="editingProduct.price" placeholder="参考价格，如 ¥9999起" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editingProduct.description" type="textarea" rows="3" placeholder="产品描述" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="product-image-uploader"
            action="http://localhost:8082/upload/product-image"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleProductImageSuccess"
            :on-error="handleProductImageError"
            :before-upload="beforeProductUpload"
          >
            <img v-if="editingProduct.image" :src="editingProduct.image" class="product-image-preview" />
            <el-icon v-else class="upload-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <!-- 根据品类显示不同的配置项 -->
        <el-divider>产品配置</el-divider>
        
        <!-- 智能手机配置 -->
        <div v-if="isPhoneCategory">
          <el-form-item label="处理器">
            <el-input v-model="specs.processor" placeholder="如 骁龙8 Gen 3" />
          </el-form-item>
          <el-form-item label="内存">
            <el-input v-model="specs.ram" placeholder="如 12GB" />
          </el-form-item>
          <el-form-item label="存储">
            <el-input v-model="specs.storage" placeholder="如 256GB" />
          </el-form-item>
          <el-form-item label="屏幕">
            <el-input v-model="specs.screen" placeholder="如 6.7英寸 OLED" />
          </el-form-item>
          <el-form-item label="电池">
            <el-input v-model="specs.battery" placeholder="如 5000mAh" />
          </el-form-item>
          <el-form-item label="摄像头">
            <el-input v-model="specs.camera" placeholder="如 5000万像素主摄" />
          </el-form-item>
        </div>
        
        <!-- 笔记本电脑配置 -->
        <div v-if="isLaptopCategory">
          <el-form-item label="处理器">
            <el-input v-model="specs.processor" placeholder="如 Intel i7-13700H" />
          </el-form-item>
          <el-form-item label="内存">
            <el-input v-model="specs.ram" placeholder="如 16GB" />
          </el-form-item>
          <el-form-item label="存储">
            <el-input v-model="specs.storage" placeholder="如 512GB SSD" />
          </el-form-item>
          <el-form-item label="显卡">
            <el-input v-model="specs.graphics" placeholder="如 RTX 4060" />
          </el-form-item>
          <el-form-item label="屏幕">
            <el-input v-model="specs.screen" placeholder="如 15.6英寸 2.5K" />
          </el-form-item>
          <el-form-item label="重量">
            <el-input v-model="specs.weight" placeholder="如 1.8kg" />
          </el-form-item>
        </div>
        
        <!-- 智能手表配置 -->
        <div v-if="isWatchCategory">
          <el-form-item label="屏幕">
            <el-input v-model="specs.screen" placeholder="如 1.5英寸 AMOLED" />
          </el-form-item>
          <el-form-item label="电池">
            <el-input v-model="specs.battery" placeholder="如 500mAh" />
          </el-form-item>
          <el-form-item label="防水">
            <el-input v-model="specs.waterproof" placeholder="如 5ATM" />
          </el-form-item>
          <el-form-item label="功能">
            <el-input v-model="specs.features" placeholder="如 心率监测、血氧" />
          </el-form-item>
        </div>
        
        <!-- 耳机配置 -->
        <div v-if="isHeadphoneCategory">
          <el-form-item label="类型">
            <el-input v-model="specs.type" placeholder="如 入耳式、头戴式" />
          </el-form-item>
          <el-form-item label="降噪">
            <el-input v-model="specs.noiseReduction" placeholder="如 主动降噪" />
          </el-form-item>
          <el-form-item label="续航">
            <el-input v-model="specs.battery" placeholder="如 30小时" />
          </el-form-item>
          <el-form-item label="连接">
            <el-input v-model="specs.connection" placeholder="如 蓝牙5.3" />
          </el-form-item>
        </div>
        
        <!-- 路由器配置 -->
        <div v-if="isRouterCategory">
          <el-form-item label="无线速率">
            <el-input v-model="specs.speed" placeholder="如 AX3000" />
          </el-form-item>
          <el-form-item label="频段">
            <el-input v-model="specs.band" placeholder="如 双频" />
          </el-form-item>
          <el-form-item label="网口">
            <el-input v-model="specs.ports" placeholder="如 4个千兆网口" />
          </el-form-item>
          <el-form-item label="带机量">
            <el-input v-model="specs.devices" placeholder="如 256台" />
          </el-form-item>
        </div>
        
        <!-- 机械键盘配置 -->
        <div v-if="isKeyboardCategory">
          <el-form-item label="轴体">
            <el-input v-model="specs.switch" placeholder="如 青轴、红轴" />
          </el-form-item>
          <el-form-item label="配列">
            <el-input v-model="specs.layout" placeholder="如 104键、87键" />
          </el-form-item>
          <el-form-item label="连接方式">
            <el-input v-model="specs.connection" placeholder="如 有线、无线" />
          </el-form-item>
          <el-form-item label="背光">
            <el-input v-model="specs.backlight" placeholder="如 RGB" />
          </el-form-item>
        </div>
        
      </el-form>
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProduct" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 品牌对话框 -->
    <el-dialog v-model="brandDialogVisible" :title="editingBrand.id ? '编辑品牌' : '添加品牌'" width="500px">
      <el-form :model="editingBrand" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editingBrand.name" placeholder="品牌名称" />
        </el-form-item>
        <el-form-item label="Logo">
          <el-upload
            class="product-image-uploader"
            action="http://localhost:8082/upload/brand-logo"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleBrandLogoSuccess"
            :on-error="handleBrandLogoError"
            :before-upload="beforeBrandLogoUpload"
          >
            <img v-if="editingBrand.logo" :src="editingBrand.logo" class="product-image-preview" />
            <el-icon v-else class="upload-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="brandDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBrand" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const defaultProductImage = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const navItems = ref([
  { id: 'topics', name: '话题管理' },
  { id: 'products', name: '产品管理' },
  { id: 'brands', name: '品牌管理' },
  { id: 'users', name: '用户管理' },
  { id: 'posts', name: '帖子管理' }
])

const currentNav = ref('topics')

const topics = ref([])
const products = ref([])
const brands = ref([])
const users = ref([])
const posts = ref([])
const categories = ref([])

const productPage = ref({
  page: 1,
  size: 10,
  total: 0,
  totalPages: 0
})
const productFilters = ref({
  categoryId: null,
  brand: '',
  keyword: ''
})

const topicDialogVisible = ref(false)
const productDialogVisible = ref(false)
const brandDialogVisible = ref(false)
const saving = ref(false)

const editingTopic = ref({ id: null, name: '', description: '', category: '' })
const editingProduct = ref({ id: null, name: '', description: '', price: '', image: '', categoryId: null, brand: '' })
const editingBrand = ref({ id: null, name: '', logo: '' })
const specs = ref({})

const uploadHeaders = ref({
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
})

const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : ''
}

const isPhoneCategory = computed(() => {
  const name = getCategoryName(editingProduct.value.categoryId)
  return name.includes('手机') || name.includes('Phone')
})

const isLaptopCategory = computed(() => {
  const name = getCategoryName(editingProduct.value.categoryId)
  return name.includes('笔记本') || name.includes('Laptop') || name.includes('电脑')
})

const isWatchCategory = computed(() => {
  const name = getCategoryName(editingProduct.value.categoryId)
  return name.includes('手表') || name.includes('Watch') || name.includes('手环')
})

const isHeadphoneCategory = computed(() => {
  const name = getCategoryName(editingProduct.value.categoryId)
  return name.includes('耳机') || name.includes('耳机')
})

const isRouterCategory = computed(() => {
  const name = getCategoryName(editingProduct.value.categoryId)
  return name.includes('路由') || name.includes('Router')
})

const isKeyboardCategory = computed(() => {
  const name = getCategoryName(editingProduct.value.categoryId)
  return name.includes('键盘') || name.includes('Keyboard')
})

const loadCategories = async () => {
  try {
    const response = await fetch('http://localhost:8082/product-categories')
    if (response.ok) {
      categories.value = await response.json()
    }
  } catch (error) {
    console.error('加载品类失败:', error)
  }
}

const loadTopics = async () => {
  try {
    const response = await fetch('http://localhost:8082/topics')
    if (response.ok) {
      topics.value = await response.json()
    }
  } catch (error) {
    console.error('加载话题失败:', error)
  }
}

const loadProducts = async () => {
  try {
    const params = new URLSearchParams()
    params.append('page', productPage.value.page)
    params.append('size', productPage.value.size)
    if (productFilters.value.categoryId) params.append('categoryId', productFilters.value.categoryId)
    if (productFilters.value.brand) params.append('brand', productFilters.value.brand)
    if (productFilters.value.keyword) params.append('keyword', productFilters.value.keyword)
    
    const response = await fetch(`http://localhost:8082/products/page?${params}`)
    if (response.ok) {
      const data = await response.json()
      products.value = data.list
      productPage.value.total = data.total
      productPage.value.totalPages = data.totalPages
    }
  } catch (error) {
    console.error('加载产品失败:', error)
  }
}

const handleProductFilterChange = () => {
  productPage.value.page = 1
  loadProducts()
}

const handleProductPageChange = (page) => {
  productPage.value.page = page
  loadProducts()
}

const loadBrands = async () => {
  try {
    const response = await fetch('http://localhost:8082/brands')
    if (response.ok) {
      brands.value = await response.json()
    }
  } catch (error) {
    console.error('加载品牌失败:', error)
  }
}

const loadUsers = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('http://localhost:8082/users', {
      headers: { Authorization: token ? `Bearer ${token}` : '' }
    })
    if (response.ok) {
      users.value = await response.json()
    }
  } catch (error) {
    console.error('加载用户失败:', error)
  }
}

const loadPosts = async () => {
  try {
    const response = await fetch('http://localhost:8082/posts')
    if (response.ok) {
      posts.value = await response.json()
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
  }
}

const showTopicDialog = (topic = null) => {
  if (topic) {
    editingTopic.value = { ...topic }
  } else {
    editingTopic.value = { id: null, name: '', description: '', category: '' }
  }
  topicDialogVisible.value = true
}

const saveTopic = async () => {
  if (!editingTopic.value.name) {
    ElMessage.warning('请输入话题名称')
    return
  }
  saving.value = true
  try {
    const token = localStorage.getItem('token')
    const url = editingTopic.value.id ? `http://localhost:8082/topics/${editingTopic.value.id}` : 'http://localhost:8082/topics'
    const method = editingTopic.value.id ? 'PUT' : 'POST'
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify(editingTopic.value)
    })
    
    if (response.ok) {
      ElMessage.success(editingTopic.value.id ? '话题更新成功' : '话题添加成功')
      topicDialogVisible.value = false
      loadTopics()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error('保存话题失败:', error)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const deleteTopicItem = async (id) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8082/topics/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': token ? `Bearer ${token}` : '' }
    })
    
    if (response.ok) {
      ElMessage.success('话题删除成功')
      loadTopics()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    console.error('删除话题失败:', error)
    ElMessage.error('删除失败')
  }
}

const showProductDialog = (product = null) => {
  if (product) {
    editingProduct.value = { 
      id: product.id,
      name: product.name,
      description: product.description,
      price: product.price,
      image: product.image,
      categoryId: product.categoryId,
      brand: product.brand
    }
    if (product.specs) {
      try {
        specs.value = typeof product.specs === 'string' ? JSON.parse(product.specs) : product.specs
      } catch {
        specs.value = {}
      }
    } else {
      specs.value = {}
    }
  } else {
    editingProduct.value = { id: null, name: '', description: '', price: '', image: '', categoryId: null, brand: '' }
    specs.value = {}
  }
  productDialogVisible.value = true
}

watch(() => editingProduct.value.categoryId, () => {
  specs.value = {}
})

const beforeProductUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt10M) ElMessage.error('图片大小不能超过10MB')
  return isImage && isLt10M
}

const handleProductImageSuccess = (response) => {
  if (response.success) {
    editingProduct.value.image = response.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleProductImageError = () => {
  ElMessage.error('图片上传失败')
}

const saveProduct = async () => {
  if (!editingProduct.value.name) {
    ElMessage.warning('请输入产品名称')
    return
  }
  if (!editingProduct.value.brand) {
    ElMessage.warning('请输入品牌')
    return
  }
  if (!editingProduct.value.categoryId) {
    ElMessage.warning('请选择品类')
    return
  }
  saving.value = true
  try {
    const token = localStorage.getItem('token')
    
    const productData = {
      ...editingProduct.value,
      specs: Object.keys(specs.value).length > 0 ? JSON.stringify(specs.value) : null
    }
    
    const url = editingProduct.value.id ? `http://localhost:8082/products/${editingProduct.value.id}` : 'http://localhost:8082/products'
    const method = editingProduct.value.id ? 'PUT' : 'POST'
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify(productData)
    })
    
    if (response.ok) {
      ElMessage.success(editingProduct.value.id ? '产品更新成功' : '产品添加成功')
      productDialogVisible.value = false
      loadProducts()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error('保存产品失败:', error)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const deleteProductItem = async (id) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8082/products/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': token ? `Bearer ${token}` : '' }
    })
    
    if (response.ok) {
      ElMessage.success('产品删除成功')
      loadProducts()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    console.error('删除产品失败:', error)
    ElMessage.error('删除失败')
  }
}

const showBrandDialog = (brand = null) => {
  if (brand) {
    editingBrand.value = { id: brand.id, name: brand.name, logo: brand.logo }
  } else {
    editingBrand.value = { id: null, name: '', logo: '' }
  }
  brandDialogVisible.value = true
}

const beforeBrandLogoUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt5M) ElMessage.error('图片大小不能超过5MB')
  return isImage && isLt5M
}

const handleBrandLogoSuccess = (response) => {
  if (response.success) {
    editingBrand.value.logo = response.url
    ElMessage.success('Logo上传成功')
  } else {
    ElMessage.error(response.message || 'Logo上传失败')
  }
}

const handleBrandLogoError = () => {
  ElMessage.error('Logo上传失败')
}

const saveBrand = async () => {
  if (!editingBrand.value.name) {
    ElMessage.warning('请输入品牌名称')
    return
  }
  saving.value = true
  try {
    const token = localStorage.getItem('token')
    const url = editingBrand.value.id ? `http://localhost:8082/brands/${editingBrand.value.id}` : 'http://localhost:8082/brands'
    const method = editingBrand.value.id ? 'PUT' : 'POST'
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify(editingBrand.value)
    })
    
    if (response.ok) {
      ElMessage.success(editingBrand.value.id ? '品牌更新成功' : '品牌添加成功')
      brandDialogVisible.value = false
      loadBrands()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error('保存品牌失败:', error)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const deleteBrandItem = async (id) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8082/brands/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': token ? `Bearer ${token}` : '' }
    })
    
    if (response.ok) {
      ElMessage.success('品牌删除成功')
      loadBrands()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    console.error('删除品牌失败:', error)
    ElMessage.error('删除失败')
  }
}

const banUserItem = async (id) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8082/users/${id}/ban`, {
      method: 'POST',
      headers: { 'Authorization': token ? `Bearer ${token}` : '' }
    })
    
    if (response.ok) {
      ElMessage.success('禁言成功')
      loadUsers()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error('禁言失败:', error)
    ElMessage.error('操作失败')
  }
}

const unbanUserItem = async (id) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8082/users/${id}/unban`, {
      method: 'POST',
      headers: { 'Authorization': token ? `Bearer ${token}` : '' }
    })
    
    if (response.ok) {
      ElMessage.success('解除禁言成功')
      loadUsers()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error('解除禁言失败:', error)
    ElMessage.error('操作失败')
  }
}

const deleteUserItem = async (id) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8082/users/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': token ? `Bearer ${token}` : '' }
    })
    
    if (response.ok) {
      ElMessage.success('用户删除成功')
      loadUsers()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    console.error('删除用户失败:', error)
    ElMessage.error('删除失败')
  }
}

const approvePostItem = async (id) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8082/posts/${id}/approve`, {
      method: 'POST',
      headers: { 'Authorization': token ? `Bearer ${token}` : '' }
    })
    
    if (response.ok) {
      ElMessage.success('审核通过')
      loadPosts()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('操作失败')
  }
}

const rejectPostItem = async (id) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8082/posts/${id}/reject`, {
      method: 'POST',
      headers: { 'Authorization': token ? `Bearer ${token}` : '' }
    })
    
    if (response.ok) {
      ElMessage.success('审核拒绝')
      loadPosts()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('操作失败')
  }
}

const deletePostItem = async (id) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`http://localhost:8082/posts/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': token ? `Bearer ${token}` : '' }
    })
    
    if (response.ok) {
      ElMessage.success('帖子删除成功')
      loadPosts()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    console.error('删除帖子失败:', error)
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadCategories()
  loadBrands()
  loadTopics()
  loadProducts()
  loadUsers()
  loadPosts()
})
</script>

<style scoped>
.admin-page {
  width: 100%;
}

.page-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  text-align: center;
}

.admin-nav {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
  background-color: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.admin-nav-btn {
  padding: 10px 24px;
  border: 1px solid #3498db;
  border-radius: 8px;
  background-color: white;
  color: #3498db;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.admin-nav-btn:hover {
  background-color: #f0f8ff;
  transform: translateY(-2px);
}

.admin-nav-btn.active {
  background-color: #3498db;
  color: white;
}

.admin-section {
  background-color: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  color: #333;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
  margin-bottom: 20px;
}

.section-header .section-title {
  border-bottom: none;
  padding-bottom: 0;
  margin-bottom: 0;
}

.product-image-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image-uploader:hover {
  border-color: #409eff;
}

.product-image-preview {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 6px;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
}
</style>
