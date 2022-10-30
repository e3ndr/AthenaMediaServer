<script>
	import SideBar from './SideBar.svelte';
	import TopBar from './TopBar.svelte';

	let isMobileSideBarOpen = false;
</script>

<div class="h-full">
	<!-- Side Bar for mobile -->
	<div
		class="relative z-40 lg:hidden"
		class:hidden={!isMobileSideBarOpen}
		role="dialog"
		aria-modal="true"
	>
		<!-- Backdrop -->
		<div class="fixed inset-0 bg-base-5 opacity-75" />

		<div class="fixed inset-0 z-40 flex">
			<SideBar />

			<button
				type="button"
				class="flex-0 w-14 flex items-top justify-center pt-8"
				on:click={() => (isMobileSideBarOpen = false)}
			>
				<span class="sr-only">Close sidebar</span>
				<icon data-icon="outline/x-mark" />
			</button>
		</div>
	</div>

	<!-- Static sidebar for desktop -->
	<div class="hidden lg:flex fixed inset-y-0 w-52 flex-col">
		<SideBar />
	</div>

	<div class="h-full flex flex-col lg:pl-52">
		<!-- Top Bar -->
		<div class="flex h-16 flex-shrink-0 border-b border-base-4 bg-base-1 lg:hidden">
			<button
				type="button"
				class="p-4 text-base-12 lg:hidden"
				on:click={() => (isMobileSideBarOpen = true)}
			>
				<span class="sr-only">Open sidebar</span>
				<icon data-icon="outline/bars-3" />
			</button>

			<TopBar />
		</div>

		<!-- Content -->
		<main class="flex-1 overflow-auto px-2 py-4 bg-base-3">
			<slot />
		</main>
	</div>
</div>
