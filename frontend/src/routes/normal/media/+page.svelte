<script>
	import AspectPoster from '$lib/aspect-ratio/AspectPoster.svelte';
	import PageTitle from '$lib/PageTitle.svelte';

	/** @type {import('./$types').PageData} */
	export let data;
</script>

<PageTitle title={['Media']} />

<div class="flex flex-row flex-wrap">
	{#each data.mediaList as media}
		<a
			class="m-2 flex flex-col w-36"
			href="/normal/media/{media.id}"
			title="{media.info.title} ({media.info.year})"
		>
			<div class="shadow-md">
				<AspectPoster>
					<img
						class="w-full h-full object-cover rounded"
						src={media.files.images.posterUrl}
						alt="{media.info.title} Poster"
					/>
				</AspectPoster>
			</div>
			<h1 class="text-sm font-medium truncate">
				{media.info.title}
			</h1>
		</a>
	{/each}
</div>
