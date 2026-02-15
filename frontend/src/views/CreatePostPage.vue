<template>
  <div class="create-post-page">
    <h2 class="page-title">发布帖子</h2>
    
    <div class="post-form card">
      <el-form :model="postForm" label-width="80px">
        <el-form-item label="选择话题">
          <el-select v-model="postForm.topicId" placeholder="请选择话题" style="width: 100%">
            <el-option v-for="topic in topics" :key="topic.id" :label="topic.name" :value="topic.id" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="帖子标题">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-form-item label="帖子内容">
          <div class="editor-container">
            <QuillEditor 
              v-model:content="postForm.content" 
              contentType="html"
              theme="snow"
              :toolbar="toolbarOptions"
              placeholder="请输入帖子内容..."
            />
          </div>
        </el-form-item>
        
        <el-form-item label="图片上传">
          <el-upload
            v-model:file-list="fileList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            list-type="picture-card"
            :limit="9"
            name="files"
            multiple
          >
            <el-icon class="upload-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传9张图片</div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitPost" :loading="submitting" size="large">发布帖子</el-button>
          <el-button @click="goBack" size="large">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '../store/user';
import topicApi from '../api/topicApi';
import postApi from '../api/postApi';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const topics = ref([]);

const postForm = ref({
  topicId: null,
  title: '',
  content: '',
  images: ''
});

const fileList = ref([]);
const uploadedUrls = ref([]);
const submitting = ref(false);

const uploadUrl = computed(() => 'http://localhost:8082/api/file/upload/images');

const uploadHeaders = computed(() => ({
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
}));

const toolbarOptions = [
  ['bold', 'italic', 'underline', 'strike'],
  ['blockquote', 'code-block'],
  [{ 'header': 1 }, { 'header': 2 }],
  [{ 'list': 'ordered' }, { 'list': 'bullet' }],
  [{ 'indent': '-1' }, { 'indent': '+1' }],
  [{ 'direction': 'rtl' }],
  [{ 'size': ['small', false, 'large', 'huge'] }],
  [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
  [{ 'color': [] }, { 'background': [] }],
  [{ 'font': [] }],
  [{ 'align': [] }],
  ['clean'],
  ['link', 'image', 'video']
];

const loadTopics = async () => {
  try {
    const response = await topicApi.getTopicList({ page: 1, pageSize: 100 });
    topics.value = response.data?.records || [];
  } catch (error) {
    console.error('加载话题失败:', error);
  }
};

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt10M = file.size / 1024 / 1024 < 10;
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件');
    return false;
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB');
    return false;
  }
  return true;
};

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    const urls = response.data || [];
    uploadedUrls.value.push(...urls);
    postForm.value.images = uploadedUrls.value.join(',');
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error(response.message || '图片上传失败');
  }
};

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请稍后重试');
};

const handleRemove = (uploadFile) => {
  const url = uploadFile.url || (uploadFile.response && uploadFile.response.data?.[0]);
  if (url) {
    const index = uploadedUrls.value.indexOf(url);
    if (index > -1) {
      uploadedUrls.value.splice(index, 1);
      postForm.value.images = uploadedUrls.value.join(',');
    }
  }
};

const submitPost = async () => {
  if (!postForm.value.topicId) {
    ElMessage.warning('请选择话题');
    return;
  }
  if (!postForm.value.title) {
    ElMessage.warning('请输入帖子标题');
    return;
  }
  if (!postForm.value.content || postForm.value.content === '<p><br></p>') {
    ElMessage.warning('请输入帖子内容');
    return;
  }
  
  submitting.value = true;
  try {
    const response = await postApi.createPost({
      topicId: postForm.value.topicId,
      title: postForm.value.title,
      content: postForm.value.content,
      images: postForm.value.images
    });
    
    ElMessage.success('帖子发布成功');
    router.push(`/post/${response.data}`);
  } catch (error) {
    console.error('发布帖子失败:', error);
    ElMessage.error('帖子发布失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  if (route.query.topicId) {
    postForm.value.topicId = parseInt(route.query.topicId);
  }
  
  loadTopics();
});
</script>

<style scoped>
.create-post-page {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
  text-align: center;
}

.card {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

:deep(.ql-container) {
  min-height: 300px;
  font-size: 16px;
}

:deep(.ql-toolbar) {
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
}

:deep(.ql-container) {
  border-bottom-left-radius: 4px;
  border-bottom-right-radius: 4px;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

:deep(.el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>
