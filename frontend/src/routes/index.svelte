<script context="module">
	import * as Api from '$lib/api.mjs';

	export async function load({ fetch }) {
		try {
			const mediaList = await Api.listMedia({ fetch });

			return {
				props: {
					mediaList
				}
			};
		} catch (e) {
			return {
				status: 500,
				error: new Error(e)
			};
		}
	}
</script>

<script>
	import PageTitle from '../components/PageTitle.svelte';
	import AspectPoster from '../components/aspect-ratio/AspectPoster.svelte';

	export let mediaList;
</script>

<PageTitle title="Media" />

<ul
	class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 xl:grid-cols-8 2xl:grid-cols-10 gap-4"
>
	{#each mediaList as mediaItem}
		<li>
			<a role="listitem" href="/media/{mediaItem.id}" title={mediaItem.info.title}>
				<AspectPoster>
					<img
						class="w-full h-full object-cover rounded"
						alt=""
						src={mediaItem.files.images.posterUrl}
					/>
				</AspectPoster>

				<span class="block text-sm truncate">
					{mediaItem.info.title}
				</span>
			</a>
		</li>
	{/each}
</ul>
