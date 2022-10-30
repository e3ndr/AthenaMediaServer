<script>
	import PageTitle from '../components/PageTitle.svelte';
	import LoadingSpinner from '../components/LoadingSpinner.svelte';
	import AspectPoster from '../components/aspect-ratio/AspectPoster.svelte';

	import * as Api from '$lib/api.mjs';
</script>

<PageTitle title="Media" />

{#await Api.listMedia()}
	<div class="h-full flex items-center justify-center">
		<div class="w-12 h-12">
			<LoadingSpinner />
		</div>
	</div>
{:then mediaList}
	<ul
		class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-10 gap-4"
	>
		{#each mediaList as mediaItem}
			<li>
				<a role="listitem" href="/media/{mediaItem.id}" title={mediaItem.info.title}>
					<AspectPoster>
						<img class="w-full h-full object-cover" alt="" src={mediaItem.files.images.posterUrl} />
					</AspectPoster>

					<span class="block text-sm truncate">
						{mediaItem.info.title}
					</span>
				</a>
			</li>
		{/each}
	</ul>
{/await}
