<script>
	import iconsHook from '$lib/icons.mjs';
	import { onMount } from 'svelte';

	// Theme colors.
	import '$lib/css/colors/base/mauve.css';
	import '$lib/css/colors/primary/crimson.css';
	import '$lib/css/colors/overlay-black.css';
	import '$lib/css/colors/overlay-white.css';

	// The actual gravy.
	import '$lib/css/app.css';
	import '$lib/css/colors/base.css';
	import '$lib/css/colors/primary.css';
	import '$lib/css/colors/misc.css';
	import '$lib/css/theme/_dark.css';

	onMount(iconsHook);
</script>

<slot />
