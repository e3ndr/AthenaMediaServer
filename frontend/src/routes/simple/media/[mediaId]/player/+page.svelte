<script>
	import PageTitle from '$lib/PageTitle.svelte';

	/** @type {import('./$types').PageData} */
	export let data;
</script>

<PageTitle title={[`${data.media.info.title} (${data.media.info.year})`]} />

<!-- svelte-ignore a11y-media-has-caption -->
<video src={data.videoUrl} controls fullscreen />
