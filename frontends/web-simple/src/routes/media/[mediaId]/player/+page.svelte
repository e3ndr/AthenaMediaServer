<script>
	import PageTitle from '$lib/PageTitle.svelte';

	/** @type {import('./$types').PageData} */
	export let data;
</script>

<PageTitle title={[`${data.media.info.title} (${data.media.info.year})`]} />

<!-- svelte-ignore a11y-media-has-caption -->
<!-- svelte-ignore a11y-missing-attribute -->
{#if data.playerType == 'FLASH'}
	<object type="application/x-shockwave-flash" data="/flv-player.swf" width="426" height="240">
		<param name="movie" value="/flv-player.swf" />
		<param name="allowFullScreen" value="true" />
		<param
			name="FlashVars"
			value="flv={encodeURIComponent(
				data.videoUrl
			)}&amp;showvolume=1&amp;showtime=2&amp;showfullscreen=1"
		/>
	</object>
{:else if data.playerType == 'SWF'}
	<object type="application/x-shockwave-flash" data={data.videoUrl} width="426" height="240" />
{:else}
	<video src={data.videoUrl} controls fullscreen style="width: 100%; height: 100%;" />
{/if}
