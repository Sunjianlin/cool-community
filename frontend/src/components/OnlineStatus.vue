<template>
  <span class="online-status" :class="statusClass">
    {{ statusText }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: Number,
    default: 0
  }
})

const statusClass = computed(() => {
  const classes = {
    0: 'offline',
    1: 'online',
    2: 'busy',
    3: 'away'
  }
  return classes[props.status] || 'offline'
})

const statusText = computed(() => {
  const texts = {
    0: '离线',
    1: '在线',
    2: '忙碌',
    3: '离开'
  }
  return texts[props.status] || '离线'
})
</script>

<style scoped>
.online-status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  margin-left: 8px;
}

.online {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.busy {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.away {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.offline {
  background-color: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}

/* 小尺寸状态指示器 */
.online-status.small {
  width: 8px;
  height: 8px;
  padding: 0;
  border-radius: 50%;
  margin-left: 4px;
}

.online-status.small.online {
  background-color: #28a745;
  border-color: #28a745;
}

.online-status.small.busy {
  background-color: #dc3545;
  border-color: #dc3545;
}

.online-status.small.away {
  background-color: #ffc107;
  border-color: #ffc107;
}

.online-status.small.offline {
  background-color: #6c757d;
  border-color: #6c757d;
}
</style>
