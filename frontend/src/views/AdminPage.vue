<template>
  <div class="admin-page">
    <h2 class="page-title">管理中心</h2>
    
    <div class="admin-nav">
      <button 
        class="admin-nav-btn" 
        v-for="navItem in navItems" 
        :key="navItem.id"
        :class="{ active: currentNav === navItem.id }"
        @click="switchNav(navItem.id)"
      >
        {{ navItem.name }}
      </button>
    </div>
    
    <div v-if="currentNav === 'topics'" class="admin-section">
      <div class="section-header">
        <h3 class="section-title">话题管理</h3>
        <button class="btn btn-primary" @click="showTopicDialog()">添加话题</button>
      </div>
      
      <div class="filter-section">
        <el-input 
          v-model="topicFilter.keyword" 
          placeholder="搜索话题名称" 
          style="width: 200px; margin-right: 10px" 
          @keyup.enter="loadTopics"
        />
        <el-button type="primary" @click="loadTopics">搜索</el-button>
      </div>
      
      <el-table :data="topics" style="width: 100%" v-loading="loading.topics">
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
      
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="topicPagination.currentPage"
          v-model:page-size="topicPagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="topicPagination.total"
          @size-change="loadTopics"
          @current-change="loadTopics"
        />
      </div>
    </div>
    
    <div v-if="currentNav === 'products'" class="admin-section">
      <div class="section-header">
        <h3 class="section-title">产品管理</h3>
        <button class="btn btn-primary" @click="showProductDialog()">添加产品</button>
      </div>
      
      <div class="filter-section">
        <el-select v-model="productFilter.categoryId" placeholder="选择分类" style="width: 150px; margin-right: 10px">
          <el-option label="全部分类" value="" />
          <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
        </el-select>
        <el-input 
          v-model="productFilter.keyword" 
          placeholder="搜索产品名称或品牌" 
          style="width: 200px; margin-right: 10px" 
          @keyup.enter="loadProducts"
        />
        <el-button type="primary" @click="loadProducts">搜索</el-button>
      </div>
      
      <el-table :data="products" style="width: 100%" v-loading="loading.products">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <img :src="scope.row.image || defaultProductImage" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px;" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column label="分类" width="120">
          <template #default="scope">
            {{ scope.row.categoryName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="showProductDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteProductItem(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="productPagination.currentPage"
          v-model:page-size="productPagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="productPagination.total"
          @size-change="loadProducts"
          @current-change="loadProducts"
        />
      </div>
    </div>
    
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
    
    <div v-if="currentNav === 'users'" class="admin-section">
      <h3 class="section-title">用户管理</h3>
      
      <!-- 在线用户统计 -->
      <div class="online-stats-section" v-if="onlineStats.totalOnline !== undefined">
        <h4 class="stats-title">在线用户统计</h4>
        <div class="stats-grid">
          <div class="stats-card">
            <div class="stats-value">{{ onlineStats.totalOnline }}</div>
            <div class="stats-label">总在线用户</div>
          </div>
          <div class="stats-card" v-for="(count, status) in onlineStats.statusCounts" :key="status">
            <div class="stats-value">{{ count }}</div>
            <div class="stats-label">{{ getStatusName(status) }}</div>
          </div>
        </div>
        
        <div v-if="onlineStats.recentUsers && onlineStats.recentUsers.length > 0" class="recent-users-section">
          <h5 class="recent-users-title">最近在线用户</h5>
          <div class="recent-users-list">
            <div class="recent-user-item" v-for="user in onlineStats.recentUsers" :key="user.id">
              <img :src="user.avatar || defaultAvatar" class="recent-user-avatar" />
              <div class="recent-user-info">
                <div class="recent-user-name">{{ user.nickname || user.username }}</div>
                <div class="recent-user-role">{{ getUserRoleName(user.role) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 用户筛选 -->
      <div class="filter-section">
        <el-select v-model="userFilter.role" placeholder="选择角色" style="width: 120px; margin-right: 10px">
          <el-option label="全部角色" value="" />
          <el-option label="普通用户" value="0" />
          <el-option label="管理员" value="1" />
        </el-select>
        <el-select v-model="userFilter.status" placeholder="选择状态" style="width: 120px; margin-right: 10px">
          <el-option label="全部状态" value="" />
          <el-option label="正常" value="1" />
          <el-option label="已禁用" value="0" />
        </el-select>
        <el-input 
          v-model="userFilter.keyword" 
          placeholder="搜索用户名或昵称" 
          style="width: 200px; margin-right: 10px" 
          @keyup.enter="loadUsers"
        />
        <el-button type="primary" @click="loadUsers">搜索</el-button>
      </div>
      
      <el-table :data="users" style="width: 100%" v-loading="loading.users">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <img :src="scope.row.avatar || defaultAvatar" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="roleName" label="角色" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="danger" size="small">已禁用</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="banUserItem(scope.row.id)" v-if="scope.row.status === 1">禁用</el-button>
            <el-button size="small" type="success" @click="unbanUserItem(scope.row.id)" v-else>解禁</el-button>
            <el-button size="small" type="danger" @click="deleteUserItem(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="userPagination.currentPage"
          v-model:page-size="userPagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="userPagination.total"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </div>
    
    <div v-if="currentNav === 'posts'" class="admin-section">
      <h3 class="section-title">帖子管理</h3>
      <el-table :data="posts" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column label="内容">
          <template #default="scope">
            <span style="display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
              {{ scope.row.content }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="作者" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="danger" size="small">已拒绝</el-tag>
            <el-tag v-else type="warning" size="small">待审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="success" @click="approvePostItem(scope.row.id)" v-if="scope.row.status !== 1">通过</el-button>
            <el-button size="small" type="danger" @click="rejectPostItem(scope.row.id)" v-if="scope.row.status !== 2">拒绝</el-button>
            <el-button size="small" type="danger" @click="deletePostItem(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <div v-if="currentNav === 'seckill'" class="admin-section">
      <div class="section-header">
        <h3 class="section-title">秒杀管理</h3>
        <button class="btn btn-primary" @click="showSeckillDialog()">添加秒杀活动</button>
      </div>
      
      <el-table :data="seckillActivities" style="width: 100%" v-loading="loading.seckill">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="activityName" label="活动名称" width="150" />
        <el-table-column prop="startTime" label="开始时间" width="200" />
        <el-table-column prop="endTime" label="结束时间" width="200" />
        <el-table-column label="背景图" width="120">
          <template #default="scope">
            <img v-if="scope.row.backgroundImage" :src="scope.row.backgroundImage" style="width: 80px; height: 80px; object-fit: cover; border-radius: 4px;" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success" size="small">进行中</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="info" size="small">已结束</el-tag>
            <el-tag v-else type="warning" size="small">未开始</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="showSeckillDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteSeckillItem(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <el-dialog v-model="topicDialogVisible" :title="editingTopic.id ? '编辑话题' : '添加话题'" width="500px">
      <el-form :model="editingTopic" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editingTopic.name" placeholder="话题名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editingTopic.description" type="textarea" rows="3" placeholder="话题描述" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editingTopic.category" placeholder="选择话题分类">
            <el-option v-for="category in topicCategories" :key="category.name" :label="category.name" :value="category.name" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topicDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTopic" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="productDialogVisible" :title="editingProduct.id ? '编辑产品' : '添加产品'" width="500px">
      <el-form :model="editingProduct" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editingProduct.name" placeholder="产品名称" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="editingProduct.brand" placeholder="选择品牌">
            <el-option v-for="brand in brands" :key="brand.id" :label="brand.name" :value="brand.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editingProduct.categoryId" placeholder="选择产品分类">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="editingProduct.price" placeholder="参考价格" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editingProduct.description" type="textarea" rows="3" placeholder="产品描述" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleProductImageSuccess"
            :before-upload="beforeUpload"
            name="file"
          >
            <img v-if="editingProduct.image" :src="editingProduct.image" class="avatar-preview" />
            <el-icon v-else class="upload-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProduct" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="brandDialogVisible" :title="editingBrand.id ? '编辑品牌' : '添加品牌'" width="500px">
      <el-form :model="editingBrand" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editingBrand.name" placeholder="品牌名称" />
        </el-form-item>
        <el-form-item label="Logo">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleBrandLogoSuccess"
            :before-upload="beforeUpload"
            name="file"
          >
            <img v-if="editingBrand.logo" :src="editingBrand.logo" class="avatar-preview" />
            <el-icon v-else class="upload-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="brandDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBrand" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="seckillDialogVisible" :title="editingSeckill.id ? '编辑秒杀活动' : '添加秒杀活动'" width="500px">
      <el-form :model="editingSeckill" label-width="80px">
        <el-form-item label="活动名称">
          <el-input v-model="editingSeckill.activityName" placeholder="活动名称" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="editingSeckill.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="editingSeckill.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="背景图">
          <el-upload
            class="avatar-uploader"
            :auto-upload="false"
            :on-change="handleSeckillBackgroundChange"
            :show-file-list="false"
            :before-upload="beforeUpload"
          >
            <img v-if="editingSeckill.backgroundImage" :src="editingSeckill.backgroundImage" class="avatar-preview" />
            <el-icon v-else class="upload-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="库存">
          <el-input v-model="editingSeckill.stock" type="number" placeholder="库存数量" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="seckillDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSeckill" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import adminApi from '../api/adminApi'
import topicApi from '../api/topicApi'
import productApi from '../api/productApi'
import brandApi from '../api/brandApi'
import categoryApi from '../api/categoryApi'
import seckillApi from '../api/seckillApi'

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const defaultProductImage = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const navItems = ref([
  { id: 'topics', name: '话题管理' },
  { id: 'products', name: '产品管理' },
  { id: 'brands', name: '品牌管理' },
  { id: 'users', name: '用户管理' },
  { id: 'posts', name: '帖子管理' },
  { id: 'seckill', name: '秒杀管理' }
])

const currentNav = ref('topics')

const topics = ref([])
const products = ref([])
const brands = ref([])
const users = ref([])
const posts = ref([])
const categories = ref([])
const topicCategories = ref([])
const seckillActivities = ref([])

const topicDialogVisible = ref(false)
const productDialogVisible = ref(false)
const brandDialogVisible = ref(false)
const seckillDialogVisible = ref(false)
const saving = ref(false)
const loading = ref({
  topics: false,
  products: false,
  brands: false,
  users: false,
  posts: false,
  seckill: false
})

const editingTopic = ref({ id: null, name: '', description: '', category: '' })
const editingProduct = ref({ id: null, name: '', description: '', price: '', image: '', brand: '', categoryId: '' })
const editingBrand = ref({ id: null, name: '', logo: '' })
const editingSeckill = ref({ id: null, activityName: '', startTime: '', endTime: '', backgroundImage: '', stock: 0 })

const topicFilter = ref({ keyword: '' })
const productFilter = ref({ categoryId: '', keyword: '' })
const userFilter = ref({ role: '', status: '', keyword: '' })

const topicPagination = ref({ currentPage: 1, pageSize: 10, total: 0 })
const productPagination = ref({ currentPage: 1, pageSize: 10, total: 0 })
const userPagination = ref({ currentPage: 1, pageSize: 20, total: 0 })

const onlineStats = ref({ totalOnline: 0, statusCounts: {}, recentUsers: [] })

const uploadUrl = computed(() => 'http://localhost:8082/api/file/upload')

const uploadHeaders = computed(() => ({
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
}))

const switchNav = (navId) => {
  currentNav.value = navId
}

const loadTopics = async () => {
  loading.value.topics = true
  try {
    const params = {
      page: topicPagination.value.currentPage,
      pageSize: topicPagination.value.pageSize,
      keyword: topicFilter.value.keyword
    }
    const response = await topicApi.getTopicList(params)
    topics.value = response.data?.records || []
    topicPagination.value.total = response.data?.total || 0
  } catch (error) {
    console.error('加载话题失败:', error)
  } finally {
    loading.value.topics = false
  }
}

const loadProducts = async () => {
  loading.value.products = true
  try {
    const params = {
      page: productPagination.value.currentPage,
      pageSize: productPagination.value.pageSize,
      categoryId: productFilter.value.categoryId || undefined,
      keyword: productFilter.value.keyword
    }
    const response = await productApi.getProductList(params)
    products.value = response.data?.records || []
    productPagination.value.total = response.data?.total || 0
  } catch (error) {
    console.error('加载产品失败:', error)
  } finally {
    loading.value.products = false
  }
}

const loadCategories = async () => {
  try {
    const response = await categoryApi.getAllCategories()
    // 检查响应结构
    if (response && typeof response === 'object') {
      // 如果响应包含data字段，使用response.data
      if (response.data && Array.isArray(response.data) && response.data.length > 0) {
        categories.value = response.data
      } 
      // 如果响应本身就是数组，直接使用
      else if (Array.isArray(response) && response.length > 0) {
        categories.value = response
      } 
      // 否则使用模拟分类
      else {
        // 使用模拟分类
        categories.value = [
          { id: 1, name: '手机' },
          { id: 2, name: '笔记本电脑' },
          { id: 3, name: '耳机' },
          { id: 4, name: '网络设备' },
          { id: 5, name: '键盘' },
          { id: 6, name: '鼠标' }
        ]
      }
    } else {
      // 使用模拟分类
      categories.value = [
        { id: 1, name: '手机' },
        { id: 2, name: '笔记本电脑' },
        { id: 3, name: '耳机' },
        { id: 4, name: '网络设备' },
        { id: 5, name: '键盘' },
        { id: 6, name: '鼠标' }
      ]
    }
  } catch (error) {
    console.error('加载产品分类失败:', error)
    // 使用模拟分类
    categories.value = [
      { id: 1, name: '手机' },
      { id: 2, name: '笔记本电脑' },
      { id: 3, name: '耳机' },
      { id: 4, name: '网络设备' },
      { id: 5, name: '键盘' },
      { id: 6, name: '鼠标' }
    ]
  }
}

const loadTopicCategories = async () => {
  try {
    // 这里假设后端有专门的话题分类API，如果没有，可以使用产品分类或硬编码一些分类
    // const response = await topicApi.getTopicCategories()
    // topicCategories.value = response.data || []
    
    // 暂时使用硬编码的话题分类
    topicCategories.value = [
      { name: '科技' },
      { name: '数码' },
      { name: '游戏' },
      { name: '生活' },
      { name: '娱乐' },
      { name: '体育' },
      { name: '财经' },
      { name: '教育' }
    ]
  } catch (error) {
    console.error('加载话题分类失败:', error)
    // 如果加载失败，使用默认分类
    topicCategories.value = [
      { name: '科技' },
      { name: '数码' },
      { name: '游戏' },
      { name: '生活' }
    ]
  }
}

const loadBrands = async () => {
  loading.value.brands = true
  try {
    const response = await brandApi.getAllBrands()
    brands.value = response.data || []
  } catch (error) {
    console.error('加载品牌失败:', error)
  } finally {
    loading.value.brands = false
  }
}

const loadUsers = async () => {
  loading.value.users = true
  try {
    const params = {
      page: userPagination.value.currentPage,
      pageSize: userPagination.value.pageSize,
      role: userFilter.value.role || undefined,
      status: userFilter.value.status || undefined,
      keyword: userFilter.value.keyword
    }
    const response = await adminApi.getUserList(params)
    users.value = response.data?.records || []
    userPagination.value.total = response.data?.total || 0
  } catch (error) {
    console.error('加载用户失败:', error)
  } finally {
    loading.value.users = false
  }
}

const loadOnlineStats = async () => {
  try {
    const response = await adminApi.getOnlineUserStats()
    onlineStats.value = response.data || { totalOnline: 0, statusCounts: {}, recentUsers: [] }
  } catch (error) {
    console.error('加载在线用户统计失败:', error)
  }
}

const loadPosts = async () => {
  loading.value.posts = true
  try {
    const response = await adminApi.getPostList({ page: 1, pageSize: 100 })
    posts.value = response.data?.records || []
  } catch (error) {
    console.error('加载帖子失败:', error)
  } finally {
    loading.value.posts = false
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
    if (editingTopic.value.id) {
      await adminApi.updateTopic(editingTopic.value)
      ElMessage.success('话题更新成功')
    } else {
      await adminApi.createTopic(editingTopic.value)
      ElMessage.success('话题添加成功')
    }
    topicDialogVisible.value = false
    loadTopics()
  } catch (error) {
    console.error('保存话题失败:', error)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const deleteTopicItem = async (id) => {
  try {
    await adminApi.deleteTopic(id)
    ElMessage.success('话题删除成功')
    loadTopics()
  } catch (error) {
    console.error('删除话题失败:', error)
    ElMessage.error('删除失败')
  }
}

const showProductDialog = (product = null) => {
  if (product) {
    editingProduct.value = { ...product }
    // 确保categoryId属性存在
    if (!editingProduct.value.categoryId) {
      // 尝试从不同的字段名获取分类ID
      editingProduct.value.categoryId = product.category_id || product.categoryId || ''
    }
  } else {
    editingProduct.value = { id: null, name: '', description: '', price: '', image: '', brand: '', categoryId: '' }
  }
  productDialogVisible.value = true
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt10M) ElMessage.error('图片大小不能超过10MB')
  return isImage && isLt10M
}

const handleProductImageSuccess = (response) => {
  if (response.code === 200) {
    editingProduct.value.image = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleBrandLogoSuccess = (response) => {
  if (response.code === 200) {
    editingBrand.value.logo = response.data
    ElMessage.success('Logo上传成功')
  } else {
    ElMessage.error(response.message || 'Logo上传失败')
  }
}

const saveProduct = async () => {
  if (!editingProduct.value.name) {
    ElMessage.warning('请输入产品名称')
    return
  }
  saving.value = true
  try {
    if (editingProduct.value.id) {
      await adminApi.updateProduct(editingProduct.value)
      ElMessage.success('产品更新成功')
    } else {
      await adminApi.createProduct(editingProduct.value)
      ElMessage.success('产品添加成功')
    }
    productDialogVisible.value = false
    loadProducts()
  } catch (error) {
    console.error('保存产品失败:', error)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const deleteProductItem = async (id) => {
  try {
    await adminApi.deleteProduct(id)
    ElMessage.success('产品删除成功')
    loadProducts()
  } catch (error) {
    console.error('删除产品失败:', error)
    ElMessage.error('删除失败')
  }
}

const showBrandDialog = (brand = null) => {
  if (brand) {
    editingBrand.value = { ...brand }
  } else {
    editingBrand.value = { id: null, name: '', logo: '' }
  }
  brandDialogVisible.value = true
}

const saveBrand = async () => {
  if (!editingBrand.value.name) {
    ElMessage.warning('请输入品牌名称')
    return
  }
  saving.value = true
  try {
    if (editingBrand.value.id) {
      await brandApi.updateBrand(editingBrand.value)
      ElMessage.success('品牌更新成功')
    } else {
      await brandApi.createBrand(editingBrand.value)
      ElMessage.success('品牌添加成功')
    }
    brandDialogVisible.value = false
    loadBrands()
  } catch (error) {
    console.error('保存品牌失败:', error)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const deleteBrandItem = async (id) => {
  try {
    await brandApi.deleteBrand(id)
    ElMessage.success('品牌删除成功')
    loadBrands()
  } catch (error) {
    console.error('删除品牌失败:', error)
    ElMessage.error('删除失败')
  }
}

const banUserItem = async (id) => {
  try {
    await adminApi.banUser(id)
    ElMessage.success('禁用成功')
    loadUsers()
  } catch (error) {
    console.error('禁用失败:', error)
    ElMessage.error('操作失败')
  }
}

const unbanUserItem = async (id) => {
  try {
    await adminApi.unbanUser(id)
    ElMessage.success('解禁成功')
    loadUsers()
  } catch (error) {
    console.error('解禁失败:', error)
    ElMessage.error('操作失败')
  }
}

const deleteUserItem = async (id) => {
  try {
    await adminApi.deleteUser(id)
    ElMessage.success('用户删除成功')
    loadUsers()
  } catch (error) {
    console.error('删除用户失败:', error)
    ElMessage.error('删除失败')
  }
}

const approvePostItem = async (id) => {
  try {
    await adminApi.approvePost(id)
    ElMessage.success('审核通过')
    loadPosts()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('操作失败')
  }
}

const rejectPostItem = async (id) => {
  try {
    await adminApi.rejectPost(id)
    ElMessage.success('审核拒绝')
    loadPosts()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('操作失败')
  }
}

const deletePostItem = async (id) => {
  try {
    await adminApi.deletePost(id)
    ElMessage.success('帖子删除成功')
    loadPosts()
  } catch (error) {
    console.error('删除帖子失败:', error)
    ElMessage.error('删除失败')
  }
}

const loadSeckillActivities = async () => {
  loading.value.seckill = true
  try {
    const response = await seckillApi.getSeckillList()
    seckillActivities.value = response.data || []
  } catch (error) {
    console.error('加载秒杀活动失败:', error)
  } finally {
    loading.value.seckill = false
  }
}

const showSeckillDialog = (seckill = null) => {
  if (seckill) {
    editingSeckill.value = { ...seckill }
  } else {
    editingSeckill.value = { id: null, activityName: '', startTime: '', endTime: '', backgroundImage: '', stock: 0 }
  }
  seckillDialogVisible.value = true
}

const handleSeckillBackgroundChange = async (file) => {
  try {
    const response = await seckillApi.uploadBackgroundImage(file.raw)
    if (response.code === 200) {
      editingSeckill.value.backgroundImage = response.data
      ElMessage.success('背景图上传成功')
    } else {
      ElMessage.error(response.message || '背景图上传失败')
    }
  } catch (error) {
    console.error('上传背景图失败:', error)
    ElMessage.error('上传失败')
  }
}

const formatDateTime = (date) => {
  if (!date) return null
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const saveSeckill = async () => {
  if (!editingSeckill.value.activityName) {
    ElMessage.warning('请输入活动名称')
    return
  }
  if (!editingSeckill.value.startTime) {
    ElMessage.warning('请选择开始时间')
    return
  }
  if (!editingSeckill.value.endTime) {
    ElMessage.warning('请选择结束时间')
    return
  }
  if (!editingSeckill.value.backgroundImage) {
    ElMessage.warning('请上传背景图')
    return
  }
  if (!editingSeckill.value.stock || editingSeckill.value.stock <= 0) {
    ElMessage.warning('请输入有效的库存数量')
    return
  }
  saving.value = true
  try {
    const dataToSend = {
      ...editingSeckill.value,
      startTime: formatDateTime(editingSeckill.value.startTime),
      endTime: formatDateTime(editingSeckill.value.endTime)
    }
    if (editingSeckill.value.id) {
      await seckillApi.updateSeckill(dataToSend)
      ElMessage.success('秒杀活动更新成功')
    } else {
      await seckillApi.createSeckill(dataToSend)
      ElMessage.success('秒杀活动添加成功')
    }
    seckillDialogVisible.value = false
    loadSeckillActivities()
  } catch (error) {
    console.error('保存秒杀活动失败:', error)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const deleteSeckillItem = async (id) => {
  try {
    await seckillApi.deleteSeckill(id)
    ElMessage.success('秒杀活动删除成功')
    loadSeckillActivities()
  } catch (error) {
    console.error('删除秒杀活动失败:', error)
    ElMessage.error('删除失败')
  }
}

const getStatusName = (status) => {
  const statusMap = {
    'ONLINE': '在线',
    'BUSY': '忙碌',
    'AWAY': '离开',
    'OFFLINE': '离线'
  }
  return statusMap[status] || status
}

const getUserRoleName = (role) => {
  return role === 1 ? '管理员' : '普通用户'
}

onMounted(() => {
  loadTopics()
  loadProducts()
  loadCategories()
  loadTopicCategories()
  loadBrands()
  loadUsers()
  loadPosts()
  loadOnlineStats()
  loadSeckillActivities()
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
  flex-wrap: wrap;
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
  margin-bottom: 30px;
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

/* 筛选区域 */
.filter-section {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

/* 分页区域 */
.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 上传区域 */
.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 6px;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
}

/* 在线用户统计 */
.online-stats-section {
  margin-bottom: 24px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.stats-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stats-card {
  background-color: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #3498db;
  margin-bottom: 8px;
}

.stats-label {
  font-size: 14px;
  color: #666;
}

.recent-users-section {
  margin-top: 20px;
}

.recent-users-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

.recent-users-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.recent-user-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.recent-user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.recent-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.recent-user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.recent-user-role {
  font-size: 12px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-nav {
    flex-direction: column;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-section .el-input {
    width: 100% !important;
    margin-right: 0 !important;
  }
  
  .filter-section .el-select {
    width: 100% !important;
    margin-right: 0 !important;
  }
  
  .pagination-section {
    justify-content: center;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .recent-users-list {
    justify-content: center;
  }
}
</style>
