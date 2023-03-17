<script>
	import { onDestroy } from 'svelte';

	const BASE = 'Athena';

	/** @type {string[]|string} */
	export let title;
	let titleString = '';

	function recalculateTitle() {
		titleString = BASE;
		let parts = title;

		if (typeof title == 'string') {
			parts = [title]; // Create an array.
		}

		for (const part of parts) {
			titleString += ' - ';
			titleString += part;
		}
	}

	$: title, recalculateTitle();

	onDestroy(() => {
		if (typeof document != 'undefined') {
			document.title = BASE;
		}
	});
</script>

<svelte:head>
	<title>
		{titleString}
	</title>
</svelte:head>
