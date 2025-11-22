\<!-- src/components/SettingsModal.vue -->
 <template>
   <div v-if="isOpen" class="modal-overlay" @click="$emit('close')">
     <div class="modal-content" @click.stop>
       <h3>Настройки</h3>
       <div class="setting-item">
         <label for="factory-select">Фабрика табулированных функций:</label>
         <select id="factory-select" v-model="selectedFactoryKey" @change="saveSettings">
           <option value="array">Массив (ArrayTabulatedFunction)</option>
           <option value="linked-list">Связный список (LinkedListTabulatedFunction)</option>
         </select>
       </div>
       <button @click="$emit('close')">Закрыть</button>
     </div>
   </div>
 </template>

 <script setup>
 import { ref, onMounted } from 'vue';

 const props = defineProps({
   isOpen: {
     type: Boolean,
     required: true,
   },
 });

 const emit = defineEmits(['close']);

 // Ключ выбранной фабрики
 const selectedFactoryKey = ref('array');

 // Загружаем настройки при монтировании
 onMounted(() => {
   const savedFactory = localStorage.getItem('tabulatedFunctionFactory');
   if (savedFactory && (savedFactory === 'array' || savedFactory === 'linked-list')) {
     selectedFactoryKey.value = savedFactory;
   }
 });

 // Сохраняем настройки в localStorage
 const saveSettings = () => {
   localStorage.setItem('tabulatedFunctionFactory', selectedFactoryKey.value);
 };
 </script>

 <style scoped>
 .modal-overlay {
   position: fixed;
   top: 0;
   left: 0;
   width: 100%;
   height: 100%;
   background-color: rgba(0, 0, 0, 0.5);
   display: flex;
   justify-content: center;
   align-items: center;
   z-index: 1000;
 }

 .modal-content {
   background-color: white;
   padding: 20px;
   border-radius: 8px;
   box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
   width: 90%;
   max-width: 500px;
   text-align: center;
 }

 .setting-item {
   margin-bottom: 20px;
   text-align: left;
 }

 label {
   display: block;
   margin-bottom: 5px;
   font-weight: bold;
 }

 select, button {
   width: 100%;
   padding: 8px;
   margin-top: 5px;
   border-radius: 4px;
   border: 1px solid #ccc;
   box-sizing: border-box;
 }

 button {
   background-color: #007bff;
   color: white;
   border: none;
   cursor: pointer;
 }

 button:hover {
   background-color: #0056b3;
 }
 </style>