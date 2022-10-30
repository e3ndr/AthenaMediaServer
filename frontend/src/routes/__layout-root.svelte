<script>
	import '$lib/css/app.css';

	import '$lib/css/theme/slate-tomato-dark.css';

	import '$lib/css/colors/base.css';
	import '$lib/css/colors/primary.css';

	import { onMount } from 'svelte';
	import * as Api from '$lib/api.mjs';

	onMount(() => {
		window.debug_Api = Api;
	});
</script>

<div id="css-intermediate" class="h-full bg-base-1 text-base-12 dark-theme">
	<slot />
</div>
