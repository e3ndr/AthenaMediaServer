<script>
	import PageTitle from '$lib/PageTitle.svelte';

	/** @type {import('./$types').PageData} */
	export let data;
</script>

<PageTitle title={['Media']} />

<div>
	{#each data.mediaList as media}
		<a
			style="margin: 10px; display: inline-block;"
			href="/simple/media/{media.id}"
			title="{media.info.title} ({media.info.year})"
		>
			<img
				style="width: 200px; height: 320px; border-radius: 20px; object-fit: cover;"
				src={media.files.images.posterUrl}
				alt="{media.info.title} Poster"
			/>
			<h1 style="font-size: small; font-weight: 500;">
				{media.info.title}
			</h1>
		</a>
	{/each}
</div>
