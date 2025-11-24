<template>
  <el-card class="function-operand">
    <template #header>
      <h3>{{ title }}</h3>
    </template>

    <el-select
      v-model="selectedFunctionId"
      placeholder="Выберите функцию"
      style="width: 100%"
      @change="handleFunctionChange"
      filterable
    >
      <el-option
        v-for="func in tabularFunctions"
        :key="func.id"
        :label="func.functionName"
        :value="func.id"
      />
    </el-select>

    <div class="operand-actions" style="margin-top: 15px">
      <el-button-group>
        <el-button @click="showCreateDialog = true">
          Создать
        </el-button>
        <el-button @click="showLoadDialog = true">
          Загрузить
        </el-button>
      </el-button-group>
    </div>

    <function-table
      v-if="selectedFunction"
      :function-id="selectedFunctionId"
      style="margin-top: 15px"
    />

    <!-- Диалоги создания -->
    <CreateFunctionDialog v-model="showCreateDialog" @created="handleFunctionCreated" />
    <CreateFromMathDialog v-model="showLoadDialog" @created="handleFunctionCreated" />
  </el-card>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useFunctionsStore } from '@/stores/functions'
import FunctionTable from '@/components/FunctionTable.vue'
import CreateFunctionDialog from '@/components/CreateFunctionDialog.vue'
import CreateFromMathDialog from '@/components/CreateFromMathDialog.vue'

const props = defineProps({
  title: String,
  functionId: Number
})

const emit = defineEmits(['functionChange', 'functionCreated'])

const functionsStore = useFunctionsStore()
const selectedFunctionId = ref(props.functionId)
const showCreateDialog = ref(false)
const showLoadDialog = ref(false)

const tabularFunctions = computed(() =>
  functionsStore.tabularFunctions
)

const selectedFunction = computed(() =>
  selectedFunctionId.value
    ? functionsStore.functions.find(f => f.id === selectedFunctionId.value)
    : null
)

watch(() => props.functionId, (newVal) => {
  selectedFunctionId.value = newVal
})

const handleFunctionChange = (functionId) => {
  selectedFunctionId.value = functionId
  emit('functionChange', functionId)
}

const handleFunctionCreated = (newFunction) => {
  functionsStore.loadFunctions()
  selectedFunctionId.value = newFunction.id
  emit('functionChange', newFunction.id)
  emit('functionCreated', newFunction)

  showCreateDialog.value = false
  showLoadDialog.value = false
}
</script>

<style scoped>
.function-operand {
  height: 100%;
}
</style>