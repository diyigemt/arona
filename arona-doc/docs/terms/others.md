<!--suppress ES6UnusedImports -->
<script setup lang="ts">
import { onMounted } from "vue";
import { scrollElementIntoViewAfterMounted } from "../utils/anchorPositioningService";

onMounted(scrollElementIntoViewAfterMounted);
</script>
