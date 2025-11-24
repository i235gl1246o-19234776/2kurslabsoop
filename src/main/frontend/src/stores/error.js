import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useErrorStore = defineStore('error', () => {
  const showError = ref(false)
  const errorTitle = ref('')
  const errorMessage = ref('')

  function showErrorModal(title, message) {
    errorTitle.value = title
    errorMessage.value = message
    showError.value = true
  }

  function clearError() {
    showError.value = false
    errorTitle.value = ''
    errorMessage.value = ''
  }

  return {
    showError,
    errorTitle,
    errorMessage,
    showErrorModal,
    clearError
  }
})